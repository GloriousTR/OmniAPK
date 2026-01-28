/*
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.search

import androidx.annotation.StringRes
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.aurora.extensions.emptyPagingItems
import com.aurora.extensions.navigate
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.store.R
import com.aurora.store.compose.composable.ContainedLoadingIndicator
import com.aurora.store.compose.composable.Error
import com.aurora.store.compose.composable.SearchSuggestionListItem
import com.aurora.store.compose.composable.app.LargeAppListItem
import com.aurora.store.compose.navigation.Screen
import com.aurora.store.compose.preview.AppPreviewProvider
import com.aurora.store.compose.preview.PreviewTemplate
import com.aurora.store.compose.ui.details.AppDetailsScreen
import com.aurora.store.data.model.SearchFilter
import com.aurora.store.data.providers.FDroidApp
import com.aurora.store.viewmodel.search.SearchViewModel
import kotlin.random.Random
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(onNavigateUp: () -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val results = viewModel.apps.collectAsLazyPagingItems()
    val fdroidApps by viewModel.fdroidApps.collectAsStateWithLifecycle()

    ScreenContent(
        suggestions = suggestions,
        results = results,
        fdroidApps = fdroidApps,
        onNavigateUp = onNavigateUp,
        onSearch = { query -> viewModel.search(query) },
        onFetchSuggestions = { query -> viewModel.fetchSuggestions(query) },
        onFilter = { filter -> viewModel.filterResults(filter) },
        isAnonymous = viewModel.authProvider.isAnonymous
    )
}

@Composable
private fun ScreenContent(
    suggestions: List<SearchSuggestEntry> = emptyList(),
    results: LazyPagingItems<App> = emptyPagingItems(),
    fdroidApps: List<FDroidApp> = emptyList(),
    onNavigateUp: () -> Unit = {},
    onFetchSuggestions: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    onFilter: (filter: SearchFilter) -> Unit = {},
    isAnonymous: Boolean = true
) {
    val context = LocalContext.current
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    var isSearching by rememberSaveable { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<String>()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = focusRequester) {
        awaitFrame()
        focusRequester.requestFocus()
    }

    LaunchedEffect(key1 = textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collectLatest { query -> onFetchSuggestions(query) }
    }

    fun showDetailPane(packageName: String) {
        coroutineScope.launch {
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, packageName)
        }
    }

    fun onRequestSearch(query: String) {
        textFieldState.setTextAndPlaceCursorAtEnd(query.trim())
        coroutineScope.launch { searchBarState.animateToCollapsed() }
        onSearch(textFieldState.text.toString())
        isSearching = true
    }

    @Composable
    fun SearchBar() {
        val inputField = @Composable {
            SearchBarDefaults.InputField(
                modifier = Modifier.focusRequester(focusRequester),
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                onSearch = { query -> onRequestSearch(query) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_hint),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                trailingIcon = {
                    if (textFieldState.text.isNotBlank()) {
                        IconButton(
                            onClick = {
                                textFieldState.clearText()
                                focusRequester.requestFocus()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cancel),
                                contentDescription = stringResource(R.string.action_clear)
                            )
                        }
                    }
                }
            )
        }

        AppBarWithSearch(state = searchBarState, inputField = inputField)
        ExpandedDockedSearchBar(state = searchBarState, inputField = inputField) {
            suggestions.forEach { suggestion ->
                SearchSuggestionListItem(
                    searchSuggestEntry = suggestion,
                    onClick = { query -> onRequestSearch(query) },
                    onAction = { query -> textFieldState.setTextAndPlaceCursorAtEnd(query.trim()) }
                )
            }
        }
    }

    @Composable
    fun ListPane() {
        // TODO: https://issuetracker.google.com/issues/445720462
        Scaffold(
            modifier = Modifier.focusable(),
            topBar = { SearchBar() }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(vertical = dimensionResource(R.dimen.padding_medium))
            ) {
                FilterHeader(
                    isEnabled = isSearching && results.loadState.refresh is LoadState.NotLoading,
                    isAnonymous = isAnonymous,
                    onFilter = onFilter
                )

                when (results.loadState.refresh) {
                    is LoadState.Loading -> ContainedLoadingIndicator()

                    is LoadState.Error -> {
                        Error(
                            modifier = Modifier.padding(paddingValues),
                            painter = painterResource(R.drawable.ic_disclaimer),
                            message = stringResource(R.string.error)
                        )
                    }

                    else -> {
                        val hasGoogleResults = results.itemCount > 0
                        val hasFdroidResults = fdroidApps.isNotEmpty()
                        
                        if (isSearching && !hasGoogleResults && !hasFdroidResults) {
                            Error(
                                modifier = Modifier.padding(paddingValues),
                                painter = painterResource(R.drawable.ic_disclaimer),
                                message = stringResource(R.string.no_apps_available)
                            )
                        } else {
                            LazyColumn {
                                // F-Droid results section
                                if (hasFdroidResults && isSearching) {
                                    item(key = "fdroid_header") {
                                        Text(
                                            text = stringResource(R.string.title_open_source),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(
                                                horizontal = dimensionResource(R.dimen.padding_medium),
                                                vertical = 8.dp
                                            )
                                        )
                                    }
                                    
                                    items(
                                        items = fdroidApps,
                                        key = { "fdroid_${it.packageName}" }
                                    ) { fdroidApp ->
                                        FDroidAppListItem(
                                            app = fdroidApp,
                                            onClick = {
                                                context.navigate(Screen.FDroidAppDetails(fdroidApp.packageName))
                                            }
                                        )
                                    }
                                    
                                    // Divider between sections
                                    if (hasGoogleResults) {
                                        item(key = "divider") {
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                                
                                // Google Play results section
                                if (hasGoogleResults) {
                                    item(key = "google_header") {
                                        Text(
                                            text = stringResource(R.string.title_google_play),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(
                                                horizontal = dimensionResource(R.dimen.padding_medium),
                                                vertical = 8.dp
                                            )
                                        )
                                    }
                                    
                                    items(
                                        count = results.itemCount,
                                        key = results.itemKey { it.id }
                                    ) { index ->
                                        results[index]?.let { app ->
                                            LargeAppListItem(
                                                app = app,
                                                onClick = { showDetailPane(app.packageName) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DetailPane() {
        with(scaffoldNavigator.currentDestination?.contentKey) {
            when {
                this != null -> {
                    AppDetailsScreen(
                        packageName = this,
                        onNavigateToAppDetails = { packageName -> showDetailPane(packageName) },
                        onNavigateUp = {
                            coroutineScope.launch { scaffoldNavigator.navigateBack() }
                        },
                        forceSinglePane = true
                    )
                }

                else -> {
                    if (isSearching && results.itemCount > 0) {
                        Error(
                            painter = painterResource(R.drawable.ic_round_search),
                            message = stringResource(R.string.select_app_for_details)
                        )
                    }
                }
            }
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = scaffoldNavigator,
        listPane = { AnimatedPane { ListPane() } },
        detailPane = { AnimatedPane { DetailPane() } }
    )
}

@Composable
private fun FilterHeader(
    isAnonymous: Boolean = true,
    isEnabled: Boolean = true,
    onFilter: (filter: SearchFilter) -> Unit
) {
    var activeFilter by rememberSaveable { mutableStateOf(SearchFilter()) }

    val filters = listOfNotNull(
        R.string.action_filter_rating,
        R.string.app_info_downloads,
        R.string.details_free,
        R.string.action_filter_no_ads,
        if (!isAnonymous) R.string.action_filter_no_gms else null
    )

    @Composable
    fun NonExpandableFilterChip(@StringRes filter: Int, isSelected: Boolean) {
        FilterChip(
            enabled = isEnabled,
            onClick = {
                activeFilter = when (filter) {
                    R.string.details_free -> activeFilter.copy(isFree = !activeFilter.isFree)
                    R.string.action_filter_no_ads -> activeFilter.copy(noAds = !activeFilter.noAds)
                    else -> activeFilter.copy(noGMS = !activeFilter.noGMS)
                }
                onFilter(activeFilter)
            },
            label = { Text(text = stringResource(filter)) },
            selected = isSelected,
            leadingIcon = {
                if (isSelected) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = stringResource(filter)
                    )
                }
            }
        )
    }

    @Composable
    fun ExpandableFilterChip(@StringRes filter: Int, isSelected: Boolean) {
        var isExpanded by rememberSaveable { mutableStateOf(false) }

        Box {
            FilterChip(
                enabled = isEnabled,
                onClick = { isExpanded = !isExpanded },
                label = { Text(text = stringResource(filter)) },
                selected = isSelected,
                leadingIcon = {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = stringResource(filter)
                        )
                    }
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_drop_down),
                        contentDescription = stringResource(filter)
                    )
                }
            )

            DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                val downloadLabels = stringArrayResource(R.array.filterDownloadsLabels)
                val downloadValues = stringArrayResource(R.array.filterDownloadsValues)
                val ratingLabels = stringArrayResource(R.array.filterRatingLabels)
                val ratingValues = stringArrayResource(R.array.filterRatingValues)

                val options = when (filter) {
                    R.string.action_filter_rating -> ratingLabels.zip(ratingValues).toMap()
                    R.string.app_info_downloads -> downloadLabels.zip(downloadValues).toMap()
                    else -> emptyMap()
                }

                options.forEach { (key, value) ->
                    DropdownMenuItem(
                        text = { Text(text = key) },
                        onClick = {
                            activeFilter = when (filter) {
                                R.string.action_filter_rating -> {
                                    activeFilter.copy(minRating = value.toFloat())
                                }

                                else -> activeFilter.copy(minInstalls = value.toLong())
                            }
                            onFilter(activeFilter)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.margin_normal))
    ) {
        items(items = filters, key = { item -> item }) { filter ->
            val isSelected = when (filter) {
                R.string.action_filter_rating -> activeFilter.minRating > 0.0
                R.string.app_info_downloads -> activeFilter.minInstalls > 0
                R.string.details_free -> activeFilter.isFree
                R.string.action_filter_no_ads -> activeFilter.noAds
                R.string.action_filter_no_gms -> activeFilter.noGMS
                else -> false
            }

            when (filter) {
                R.string.details_free,
                R.string.action_filter_no_ads,
                R.string.action_filter_no_gms -> {
                    NonExpandableFilterChip(filter = filter, isSelected = isSelected)
                }

                R.string.action_filter_rating,
                R.string.app_info_downloads -> {
                    ExpandableFilterChip(filter = filter, isSelected = isSelected)
                }
            }
        }
    }
}

/**
 * Composable to display an F-Droid app in the search results list
 */
@Composable
private fun FDroidAppListItem(
    app: FDroidApp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.padding_medium),
                vertical = 4.dp
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(app.iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = app.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = painterResource(R.drawable.ic_app_placeholder),
                error = painterResource(R.drawable.ic_app_placeholder)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = app.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "v${app.versionName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = app.repoName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun SearchScreenPreview(@PreviewParameter(AppPreviewProvider::class) app: App) {
    PreviewTemplate {
        val apps = List(10) { app.copy(id = Random.nextInt()) }
        val results = MutableStateFlow(PagingData.from(apps)).collectAsLazyPagingItems()
        ScreenContent(results = results)
    }
}
