# NEXUS.AI API protocol

Base URL: `https://nexus.ai-nex.duckdns.org`

- `POST /api/auth` with `{"action":"login","email":"...","password":"..."}`
- `POST /api/auth` with `{"action":"signup","email":"...","password":"..."}`
- Successful auth returns `access_token`, `refresh_token`, and user data when Supabase returns a session.
- `POST /api/chat` with `Authorization: Bearer <access_token>` and `{"message":"...","characterPrompt":"..." }`
- Chat result uses `reply`; backend errors use `error`.

Never put `GROQ_API_KEY` or the Supabase service-role key in the mod. Credits are consumed by the NEXUS.AI backend, so web and mod use the same server-side credit account. Signup may not return a session if email confirmation is enabled.
