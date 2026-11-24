package com.slouka.app.ui.screens.home.parts

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slouka.app.R
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.words.WordSearchRepository
import com.slouka.app.words.WordSearchResult
import kotlinx.coroutines.launch

private val SearchBarMotion = tween<Float>(420, easing = FastOutSlowInEasing)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeWordSearch(
    query: String,
    results: List<WordSearchResult>,
    onQueryChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val searchBarState = rememberSearchBarState(
        animationSpecForExpand = SearchBarMotion,
        animationSpecForCollapse = SearchBarMotion
    )
    val textFieldState = rememberTextFieldState(initialText = query)
    val dispatchQuery by rememberUpdatedState(onQueryChange)
    val scope = rememberCoroutineScope()
    val searchLabel = stringResource(R.string.home_search_hint)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collect { dispatchQuery(it) }
    }

    SearchBar(
        state = searchBarState,
        inputField = {
            WordSearchInput(searchBarState, textFieldState, enabled)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = {
            WordSearchInput(searchBarState, textFieldState, enabled, onBack = {
                scope.launch { searchBarState.animateToCollapsed() }
            })
        },
        modifier = Modifier.semantics { paneTitle = searchLabel }
    ) {
        WordSearchResults(query, results)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordSearchInput(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    enabled: Boolean,
    onBack: (() -> Unit)? = null
) {
    val keyboard = LocalSoftwareKeyboardController.current
    SearchBarDefaults.InputField(
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        onSearch = { keyboard?.hide() },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = onBack == null,
        placeholder = { Text(stringResource(R.string.home_search_hint)) },
        leadingIcon = {
            if (onBack == null) {
                Icon(Icons.Rounded.Search, contentDescription = null)
            } else {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back))
                }
            }
        },
        trailingIcon = {
            if (textFieldState.text.isNotEmpty()) {
                IconButton(enabled = enabled, onClick = { textFieldState.clearText() }) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.home_search_clear))
                }
            }
        }
    )
}

@Composable
private fun WordSearchResults(query: String, results: List<WordSearchResult>) {
    val modifier = Modifier.fillMaxWidth().imePadding()

    if (results.isNotEmpty()) {
        LazyColumn(modifier) {
            items(results) { result ->
                ListItem(
                    headlineContent = { Text(result.word) },
                    supportingContent = { Text(result.translation) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    } else if (query.isNotBlank()) {
        Text(
            text = stringResource(R.string.home_search_no_results),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WordSearchResultsPreview() {
    SloukaTheme {
        val query = stringResource(R.string.preview_search_query)
        Surface {
            WordSearchResults(
                query = query,
                results = WordSearchRepository().search(query)
            )
        }
    }
}
