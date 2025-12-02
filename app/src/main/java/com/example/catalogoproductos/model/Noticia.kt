package com.example.catalogoproductos.model

import com.google.gson.annotations.SerializedName

data class NoticiaSource(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("country") val country: String?
)

data class Noticia(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("lang") val lang: String?,
    @SerializedName("source") val source: NoticiaSource?
)

data class GNewsResponse(
    @SerializedName("totalArticles") val totalArticles: Int?,
    @SerializedName("articles") val articles: List<Noticia>?
)

