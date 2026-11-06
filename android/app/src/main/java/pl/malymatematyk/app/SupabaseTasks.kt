package pl.malymatematyk.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal object SupabaseTasks {
    // Publishable key is designed for client-side use. Row Level Security controls access.
    private const val PROJECT_URL = "https://sgisjcidclmpvgxzazeq.supabase.co"
    private const val PUBLISHABLE_KEY = "sb_publishable_o3Nc0czIx8SXOiEFokLIZg_JO_MQc2N"
    private const val PREFS = "maly_matematyk"
    private const val CACHE_KEY = "remote_tasks_cache"

    fun loadCached(context: Context): List<Question> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(CACHE_KEY, null)
            ?: return emptyList()
        return try { parseTasks(JSONArray(raw)) } catch (_: Exception) { emptyList() }
    }

    fun fetchAndCache(context: Context): List<Question> {
        val endpoint = "$PROJECT_URL/rest/v1/tasks?select=grade,track,question,answers,correct_answer,hint,explanation&published=eq.true&order=created_at.desc"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12000
            readTimeout = 12000
            setRequestProperty("apikey", PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $PUBLISHABLE_KEY")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IllegalStateException("Supabase HTTP $code")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val parsed = parseTasks(JSONArray(body))
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(CACHE_KEY, body).apply()
            return parsed
        } finally {
            connection.disconnect()
        }
    }

    private fun parseTasks(rows: JSONArray): List<Question> {
        val result = mutableListOf<Question>()
        for (i in 0 until rows.length()) {
            val row = rows.optJSONObject(i) ?: continue
            val grade = row.optInt("grade", 0)
            if (grade !in 1..8) continue
            val track = when (row.optString("track")) {
                "kangur" -> Track.KANGAROO
                "leon" -> Track.LEON
                "alemozgi" -> Track.ALE_MOZGI
                "python_algorytmika" -> Track.PYTHON
                "matematyka_logika" -> Track.MATH
                else -> Track.MATH
            }
            val answersJson = row.optJSONArray("answers") ?: JSONArray()
            val answers = (0 until answersJson.length()).map { answersJson.optString(it) }
            if (answers.size < 2) continue
            val correctText = row.optString("correct_answer")
            val correctIndex = answers.indexOf(correctText)
            if (correctIndex < 0) continue
            result += Question(
                grade = grade,
                track = track,
                prompt = row.optString("question"),
                options = answers,
                answer = correctIndex,
                hint = row.optString("hint"),
                explanation = row.optString("explanation")
            )
        }
        return result
    }
}
