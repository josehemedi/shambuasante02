/**
 * Verifie les credentials LiveKit (API REST) sans afficher de secrets.
 */
import { readFileSync } from "fs"
import { RoomServiceClient } from "livekit-server-sdk"

const data = JSON.parse(
  readFileSync(new URL("./.tele-test-data.json", import.meta.url), "utf8")
)

const apiKey = process.env.LIVEKIT_API_KEY
const apiSecret = process.env.LIVEKIT_API_SECRET
if (!apiKey || !apiSecret) {
  console.error("[FAIL] LIVEKIT_API_KEY et LIVEKIT_API_SECRET requis en variables d'environnement")
  process.exit(1)
}

const host = data.serverUrl.replace(/^wss:\/\//, "https://")
const client = new RoomServiceClient(host, apiKey, apiSecret)

try {
  const rooms = await client.listRooms()
  const target = rooms.find((r) => r.name === data.roomName)
  console.log("[OK] API LiveKit accessible")
  console.log(`[INFO] Salles actives: ${rooms.length}`)
  console.log(`[INFO] Salle cible ${data.roomName}: ${target ? "existe" : "pas encore creee (normal avant join)"}`)
  process.exit(0)
} catch (err) {
  console.error("[FAIL] API LiveKit:", err.message)
  process.exit(1)
}
