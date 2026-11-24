package com.slouka.app.words

import javax.inject.Inject

class WordSearchRepository @Inject constructor() {
    fun search(query: String): List<WordSearchResult> {
        val term = query.trim()
        if (term.isEmpty()) return emptyList()

        return sampleWords.filter {
            it.word.contains(term, ignoreCase = true) || it.translation.contains(term, ignoreCase = true)
        }
    }

    private companion object {
        val sampleWords = listOf(
            WordSearchResult("apple", "яблоко"),
            WordSearchResult("apricot", "абрикос"),
            WordSearchResult("book", "книга"),
            WordSearchResult("bookstore", "книжный магазин"),
            WordSearchResult("bread", "хлеб"),
            WordSearchResult("coffee", "кофе"),
            WordSearchResult("flower", "цветок"),
            WordSearchResult("hello", "привет"),
            WordSearchResult("library", "библиотека"),
            WordSearchResult("pineapple", "ананас"),
            WordSearchResult("water", "вода")
        )
    }
}
