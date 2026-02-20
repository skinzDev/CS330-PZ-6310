package com.example.dadada.Repository

import android.content.Context
import com.example.dadada.Domain.CastModel
import com.example.dadada.Domain.FilmItemModel
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class CartRepository(
    context: Context,
    userScope: String
) {
    private val scope = userScope.trim()
    private val prefs = context.getSharedPreferences(prefsNameFor(scope), Context.MODE_PRIVATE)

    init {
        migrateDataIfNeeded(context, scope)
    }

    fun getCartItems(): List<FilmItemModel> {
        val raw = prefs.getString(KEY_ITEMS, "[]") ?: "[]"
        val jsonArray = try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }

        val items = mutableListOf<FilmItemModel>()
        for (index in 0 until jsonArray.length()) {
            val json = jsonArray.optJSONObject(index) ?: continue
            items.add(fromJson(json))
        }
        return items
    }

    fun isInCart(item: FilmItemModel): Boolean {
        val targetKey = filmKey(item)
        return getCartItems().any { filmKey(it) == targetKey }
    }

    fun addToCart(item: FilmItemModel): Boolean {
        val items = getCartItems().toMutableList()
        val targetKey = filmKey(item)
        if (items.any { filmKey(it) == targetKey }) {
            return false
        }

        items.add(item)
        saveCartItems(items)
        return true
    }

    fun removeFromCart(item: FilmItemModel): Boolean {
        val items = getCartItems().toMutableList()
        val targetKey = filmKey(item)
        val removed = items.removeAll { filmKey(it) == targetKey }
        if (removed) {
            saveCartItems(items)
        }
        return removed
    }

    fun clearCart() {
        prefs.edit()
            .putString(KEY_ITEMS, "[]")
            .apply()
    }

    private fun saveCartItems(items: List<FilmItemModel>) {
        val jsonArray = JSONArray()
        items.forEach { item ->
            jsonArray.put(toJson(item))
        }

        prefs.edit()
            .putString(KEY_ITEMS, jsonArray.toString())
            .apply()
    }

    private fun toJson(item: FilmItemModel): JSONObject {
        val genreJson = JSONArray()
        item.Genre.forEach { genreJson.put(it) }

        val castJson = JSONArray()
        item.Casts.forEach { cast ->
            castJson.put(
                JSONObject()
                    .put("PicUrl", cast.PicUrl)
                    .put("Actor", cast.Actor)
            )
        }

        return JSONObject()
            .put("Title", item.Title)
            .put("Description", item.Description)
            .put("Poster", item.Poster)
            .put("Time", item.Time)
            .put("Year", item.Year)
            .put("Imdb", item.Imdb)
            .put("price", item.price)
            .put("Genre", genreJson)
            .put("Casts", castJson)
    }

    private fun fromJson(json: JSONObject): FilmItemModel {
        val genre = ArrayList<String>()
        val genreJson = json.optJSONArray("Genre") ?: JSONArray()
        for (index in 0 until genreJson.length()) {
            genre.add(genreJson.optString(index))
        }

        val casts = ArrayList<CastModel>()
        val castJson = json.optJSONArray("Casts") ?: JSONArray()
        for (index in 0 until castJson.length()) {
            val castObject = castJson.optJSONObject(index) ?: continue
            casts.add(
                CastModel(
                    PicUrl = castObject.optString("PicUrl"),
                    Actor = castObject.optString("Actor")
                )
            )
        }

        return FilmItemModel(
            Title = json.optString("Title"),
            Description = json.optString("Description"),
            Poster = json.optString("Poster"),
            Time = json.optString("Time"),
            Year = json.optInt("Year"),
            Imdb = json.optDouble("Imdb"),
            price = json.optDouble("price"),
            Genre = genre,
            Casts = casts
        )
    }

    private fun filmKey(item: FilmItemModel): String {
        val title = item.Title.trim().lowercase()
        val poster = item.Poster.trim().lowercase()
        return "$title|${item.Year}|$poster"
    }

    companion object {
        private const val LEGACY_PREFS_NAME = "cart_prefs"
        private const val KEY_ITEMS = "cart_items"
        private const val HEX_CHARS = "0123456789abcdef"

        private fun prefsNameFor(userScope: String): String {
            return "${LEGACY_PREFS_NAME}_${userKey(userScope)}"
        }

        private fun userKey(userScope: String): String {
            val normalized = userScope.trim()
            if (normalized.isBlank()) return "guest"
            return sha256(normalized)
        }

        private fun oldPrefsNameFor(userScope: String): String {
            return "${LEGACY_PREFS_NAME}_${legacyUserKey(userScope)}"
        }

        private fun legacyUserKey(userScope: String): String {
            val normalized = userScope
                .trim()
                .lowercase()
                .replace(Regex("[^a-z0-9_]"), "_")
            return normalized.ifBlank { "guest" }
        }

        private fun sha256(value: String): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
            val out = StringBuilder(digest.size * 2)
            for (byte in digest) {
                val intValue = byte.toInt() and 0xFF
                out.append(HEX_CHARS[intValue ushr 4])
                out.append(HEX_CHARS[intValue and 0x0F])
            }
            return out.toString()
        }
    }

    private fun migrateDataIfNeeded(context: Context, userScope: String) {
        if (prefs.contains(KEY_ITEMS)) return

        val newName = prefsNameFor(userScope)
        val oldScopedName = oldPrefsNameFor(userScope)
        if (oldScopedName != newName) {
            val oldScopedPrefs = context.getSharedPreferences(oldScopedName, Context.MODE_PRIVATE)
            if (oldScopedPrefs.contains(KEY_ITEMS)) {
                val scopedItems = oldScopedPrefs.getString(KEY_ITEMS, "[]") ?: "[]"
                prefs.edit()
                    .putString(KEY_ITEMS, scopedItems)
                    .apply()
                oldScopedPrefs.edit()
                    .remove(KEY_ITEMS)
                    .apply()
                return
            }
        }

        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        if (!legacyPrefs.contains(KEY_ITEMS)) return

        val legacyItems = legacyPrefs.getString(KEY_ITEMS, "[]") ?: "[]"
        prefs.edit()
            .putString(KEY_ITEMS, legacyItems)
            .apply()

        legacyPrefs.edit()
            .remove(KEY_ITEMS)
            .apply()
    }
}
