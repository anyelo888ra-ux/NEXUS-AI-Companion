package ai.nexus.companion;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Consumer;

final class NexusSession {
    private static final String API = "https://nexus.ai-nex.duckdns.org";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private static volatile String accessToken = "";
    private static volatile String refreshToken = "";
    private static volatile String activeTicket = "";
    private static volatile boolean cancelLoginRequested = false;

    private NexusSession() {}

    static boolean isLoggedIn() { return !accessToken.isBlank(); }

    static void load() {
        try {
            Path path = sessionPath();
            if (Files.exists(path)) {
                JsonObject saved = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                accessToken = saved.has("access_token") ? saved.get("access_token").getAsString() : "";
                refreshToken = saved.has("refresh_token") ? saved.get("refresh_token").getAsString() : "";
            }
        } catch (Exception ignored) { accessToken = ""; refreshToken = ""; }
    }

    static void browserLogin(Consumer<String> callback) {
        callback.accept("Solicitando un ticket temporal de vinculación…");
        new Thread(() -> {
            try {
                JsonObject create = new JsonObject();
                create.addProperty("action", "create");
                JsonObject result = post("/api/device-auth", create, null);
                String ticket = result.has("ticket") ? result.get("ticket").getAsString() : "";
                activeTicket = ticket;
                cancelLoginRequested = false;
                String loginUrl = result.has("login_url") ? result.get("login_url").getAsString() : "";
                if (ticket.length() < 32 || loginUrl.isBlank()) throw new IOException("El servidor no devolvió un ticket válido.");
                String url = loginUrl + "?ticket=" + URLEncoder.encode(ticket, StandardCharsets.UTF_8);
                try {
                    if (!Desktop.isDesktopSupported()) throw new IOException("Este sistema no permite abrir el navegador automáticamente.");
                    Desktop.getDesktop().browse(URI.create(url));
                } catch (Exception browserError) {
                    callback.accept("Abre este enlace en tu navegador para vincular la cuenta: " + url);
                }
                callback.accept("Se abrió la página de NEXUS.AI. Inicia sesión allí; el ticket caduca en 5 minutos.");
                long deadline = System.currentTimeMillis() + Duration.ofMinutes(5).toMillis();
                while (System.currentTimeMillis() < deadline) {
                    if (cancelLoginRequested || !ticket.equals(activeTicket)) {
                        cancelTicketOnServer(ticket);
                        callback.accept("Ticket temporal eliminado al salir del mundo. La sesión de NEXUS.AI se conserva.");
                        return;
                    }
                    Thread.sleep(2000);
                    JsonObject poll = new JsonObject();
                    poll.addProperty("action", "status");
                    poll.addProperty("ticket", ticket);
                    JsonObject status = post("/api/device-auth", poll, null);
                    String state = status.has("status") ? status.get("status").getAsString() : "pending";
                    if ("approved".equals(state)) {
                        if (!status.has("access_token") || status.get("access_token").isJsonNull()) throw new IOException("La autorización no devolvió una sesión.");
                        accessToken = status.get("access_token").getAsString();
                        refreshToken = status.has("refresh_token") && !status.get("refresh_token").isJsonNull()
                            ? status.get("refresh_token").getAsString() : "";
                        save();
                        activeTicket = "";
                        callback.accept("¡NEXUS.AI conectado! Ya puedes usar /nexus chat <mensaje>.");
                        return;
                    }
                    if ("expired".equals(state)) {
                        callback.accept("El ticket expiró. Ejecuta /nexus login para intentarlo de nuevo.");
                        return;
                    }
                }
                cancelTicketOnServer(ticket);
                activeTicket = "";
                callback.accept("Se agotó el tiempo de vinculación. Ejecuta /nexus login otra vez.");
            } catch (Exception e) {
                activeTicket = "";
                callback.accept("No se pudo vincular la cuenta: " + safeMessage(e));
            }
        }, "NEXUS-device-login").start();
    }

    /** Kept for compatibility with older callers; credentials are never accepted in Minecraft. */
    static void authenticate(String action, String email, String password, Consumer<String> callback) {
        callback.accept("Por seguridad, ya no se aceptan contraseñas dentro de Minecraft. Usa /nexus login y completa el acceso en tu navegador.");
    }

    static void chat(String message, Consumer<String> callback) {
        if (!isLoggedIn()) {
            callback.accept("Primero inicia sesión: /nexus login (se abrirá el navegador).");
            return;
        }
        if (message.length() > 4000) {
            callback.accept("El mensaje supera los 4000 caracteres.");
            return;
        }
        callback.accept("Pensando…");
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("message", message);
                body.addProperty("characterPrompt", "You are the friendly NEXUS Minecraft companion: curious, kind, funny, and concise. Help the player safely. Do not claim to perform Minecraft actions; this chat version cannot control the world.");
                JsonObject result = post("/api/chat", body, accessToken);
                if (result.has("error")) {
                    callback.accept(result.get("error").getAsString());
                    return;
                }
                String reply = result.has("reply") ? result.get("reply").getAsString() : "El servidor no devolvió una respuesta.";
                if (reply.length() > 1500) reply = reply.substring(0, 1500) + "…";
                callback.accept(reply);
            } catch (Exception e) {
                callback.accept("Error hablando con NEXUS.AI: " + safeMessage(e));
            }
        }, "NEXUS-chat").start();
    }

    static void cancelBrowserLogin() {
        cancelLoginRequested = true;
        String ticket = activeTicket;
        activeTicket = "";
        if (!ticket.isBlank()) new Thread(() -> cancelTicketOnServer(ticket), "NEXUS-cancel-ticket").start();
    }

    private static void cancelTicketOnServer(String ticket) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("action", "cancel");
            body.addProperty("ticket", ticket);
            post("/api/device-auth", body, null);
        } catch (Exception ignored) { /* Server-side expiry remains the fallback cleanup. */ }
    }

    static void logout() {
        cancelBrowserLogin();
        accessToken = "";
        refreshToken = "";
        try { Files.deleteIfExists(sessionPath()); } catch (IOException ignored) {}
    }

    private static JsonObject post(String path, JsonObject body, String bearer) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API + path))
            .timeout(Duration.ofSeconds(45))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));
        if (bearer != null && !bearer.isBlank()) builder.header("Authorization", "Bearer " + bearer);
        HttpResponse<String> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject json;
        try { json = JsonParser.parseString(response.body()).getAsJsonObject(); }
        catch (Exception e) { throw new IOException("Respuesta no válida del servidor (HTTP " + response.statusCode() + ")."); }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(json.has("error") ? json.get("error").getAsString() : "HTTP " + response.statusCode());
        }
        return json;
    }

    private static void save() throws IOException {
        Path path = sessionPath();
        Files.createDirectories(path.getParent());
        JsonObject json = new JsonObject();
        json.addProperty("access_token", accessToken);
        json.addProperty("refresh_token", refreshToken);
        Files.writeString(path, json.toString(), StandardCharsets.UTF_8);
    }

    private static Path sessionPath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("nexus-companion-session.json");
    }

    private static String safeMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) return e.getClass().getSimpleName();
        return message.length() > 180 ? message.substring(0, 180) : message;
    }
}
