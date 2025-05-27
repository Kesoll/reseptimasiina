package com.example.reseptimasiina

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): RecipeEntity?

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeEntity>

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Int): Int  // optionally return number of rows deleted

    @Query("DELETE FROM recipes")
    suspend fun deleteAll(): Int
}


