package com.example.arte_del_monte

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.arte_del_monte.data.model.DocumentType
import com.example.arte_del_monte.ui.screen.*
import com.example.arte_del_monte.ui.theme.Arte_del_monteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Arte_del_monteTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onNewDocument = { type ->
                                navController.navigate("editor?type=${type.name}")
                            },
                            onOpenDocument = { id ->
                                navController.navigate("editor?id=$id")
                            },
                            onOpenHistory = {
                                navController.navigate("history")
                            },
                            onOpenSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    composable("history") {
                        DocumentListScreen(
                            onBack = { navController.popBackStack() },
                            onOpenDocument = { id ->
                                navController.navigate("editor?id=$id")
                            },
                            onDuplicateDocument = { id ->
                                navController.navigate("editor?id=$id")
                            }
                        )
                    }

                    composable(
                        route = "editor?id={id}&type={type}",
                        arguments = listOf(
                            navArgument("id") { type = NavType.LongType; defaultValue = 0L },
                            navArgument("type") { type = NavType.StringType; nullable = true }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getLong("id") ?: 0L
                        val typeStr = backStackEntry.arguments?.getString("type")
                        val docType = try {
                            if (typeStr != null) DocumentType.valueOf(typeStr) else DocumentType.COTIZACION
                        } catch (e: Exception) { DocumentType.COTIZACION }

                        DocumentEditorScreen(
                            documentId = id,
                            initialType = docType,
                            onBack = { navController.popBackStack() },
                            onSaved = { savedId ->
                                navController.navigate("preview/$savedId") {
                                    popUpTo("editor?id=$id&type=$typeStr") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = "preview/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getLong("id") ?: 0L
                        DocumentPreviewScreen(
                            documentId = id,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        BrandSettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}