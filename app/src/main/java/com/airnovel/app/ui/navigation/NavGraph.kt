package com.airnovel.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airnovel.app.data.local.ChapterCache
import com.airnovel.app.ui.bookshelf.BookshelfScreen
import com.airnovel.app.ui.chapters.ChapterListScreen
import com.airnovel.app.ui.reader.ReaderScreen
import com.airnovel.app.ui.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val SETTINGS = "settings"
    const val SETTINGS_FIRST = "settings_first"
    const val BOOKSHELF = "bookshelf"
    const val CHAPTER_LIST = "chapter_list/{bookId}/{bookTitle}/{bookDescription}"
    const val READER = "reader/{bookId}/{chapterId}/{bookTitle}/{chapterIndex}"

    fun chapterList(bookId: String, bookTitle: String, bookDescription: String = ""): String {
        val encodedTitle = URLEncoder.encode(bookTitle, "UTF-8")
        val encodedDesc = URLEncoder.encode(bookDescription, "UTF-8")
        return "chapter_list/$bookId/$encodedTitle/$encodedDesc"
    }

    fun reader(
        bookId: String,
        chapterId: Int,
        bookTitle: String,
        chapterIndex: Int
    ): String {
        val encodedTitle = URLEncoder.encode(bookTitle, "UTF-8")
        return "reader/$bookId/$chapterId/$encodedTitle/$chapterIndex"
    }
}

@Composable
fun AirNovelNavGraph(
    navController: NavHostController,
    startDestination: String,
    isFirstLaunch: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SETTINGS_FIRST) {
            SettingsScreen(
                isFirstLaunch = true,
                onNavigateBack = { navController.popBackStack() },
                onSettingsSaved = {
                    navController.navigate(Routes.BOOKSHELF) {
                        popUpTo(Routes.SETTINGS_FIRST) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                isFirstLaunch = false,
                onNavigateBack = { navController.popBackStack() },
                onSettingsSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.BOOKSHELF) {
            BookshelfScreen(
                onBookClick = { bookId, bookTitle, bookDescription ->
                    navController.navigate(Routes.chapterList(bookId, bookTitle, bookDescription))
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(
            route = Routes.CHAPTER_LIST,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("bookTitle") { type = NavType.StringType },
                navArgument("bookDescription") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            val bookTitle = URLDecoder.decode(
                backStackEntry.arguments?.getString("bookTitle") ?: "", "UTF-8"
            )
            val bookDescription = URLDecoder.decode(
                backStackEntry.arguments?.getString("bookDescription") ?: "", "UTF-8"
            )
            ChapterListScreen(
                bookId = bookId,
                bookTitle = bookTitle,
                bookDescription = bookDescription,
                onNavigateBack = { navController.popBackStack() },
                onChapterClick = { id, chapterId, title, index ->
                    navController.navigate(
                        Routes.reader(id, chapterId, title, index)
                    )
                }
            )
        }

        composable(
            route = Routes.READER,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.IntType },
                navArgument("bookTitle") { type = NavType.StringType },
                navArgument("chapterIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 0
            val bookTitle = URLDecoder.decode(
                backStackEntry.arguments?.getString("bookTitle") ?: "", "UTF-8"
            )
            val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex") ?: 0
            val chapterIds = ChapterCache.getChapterIds(bookId)

            ReaderScreen(
                bookId = bookId,
                chapterId = chapterId.toString(),
                bookTitle = bookTitle,
                chapterIndex = chapterIndex,
                chapterIdList = chapterIds,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
