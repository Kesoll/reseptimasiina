import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reseptimasiina.R
import com.example.reseptimasiina.RecipeEntity

/**
 * RecyclerView adapter for displaying a list of recipes in an efficient, scrollable format.
 *
 * Uses ListAdapter with DiffUtil for optimal performance when updating recipe lists.
 * Features:
 * - Automatic list diffing for smooth animations and minimal UI updates
 * - Click handling for recipe item selection
 * - Clean ViewHolder pattern for efficient view recycling
 * - Support for displaying recipe title and description
 *
 * @param onItemClick Callback function invoked when a recipe item is tapped
 */
class RecipeAdapter(
    private val onItemClick: (RecipeEntity) -> Unit
) : ListAdapter<RecipeEntity, RecipeAdapter.RecipeViewHolder>(DIFF_CALLBACK) {

    companion object {
        /**
         * DiffUtil callback that defines how to compare RecipeEntity objects.
         *
         * This enables efficient list updates by calculating the minimal set of changes
         * needed when the recipe list is modified, resulting in:
         * - Smooth animations when items are added/removed/moved
         * - Better performance by avoiding full list redraws
         * - Preserved scroll position during updates
         */
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecipeEntity>() {
            /**
             * Determines if two recipe items represent the same entity.
             * Used to identify moved items in the list.
             *
             * @param oldItem The recipe from the previous list
             * @param newItem The recipe from the new list
             * @return true if both items have the same unique identifier
             */
            override fun areItemsTheSame(oldItem: RecipeEntity, newItem: RecipeEntity) = oldItem.id == newItem.id

            /**
             * Determines if the content of two recipe items is identical.
             * Used to decide whether to update the UI for an item.
             *
             * @param oldItem The recipe from the previous list
             * @param newItem The recipe from the new list
             * @return true if all displayed content is identical
             */
            override fun areContentsTheSame(oldItem: RecipeEntity, newItem: RecipeEntity) = oldItem == newItem
        }
    }

    /**
     * Creates new ViewHolder instances when RecyclerView needs them.
     *
     * This method is called when RecyclerView needs a new ViewHolder to represent an item.
     * The ViewHolder will be reused for different items as the user scrolls.
     *
     * @param parent The parent ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (unused in this simple adapter)
     * @return A new RecipeViewHolder instance
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    /**
     * Binds recipe data to an existing ViewHolder.
     *
     * Called when RecyclerView needs to display data at a specific position.
     * This method updates the ViewHolder's views with the appropriate recipe data.
     *
     * @param holder The ViewHolder to update
     * @param position The position of the item within the adapter's data set
     */
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        holder.bind(recipe)
    }

    /**
     * ViewHolder class that holds references to views within each recipe list item.
     *
     * Implements the ViewHolder pattern for efficient view recycling in RecyclerView.
     * Maintains references to UI components to avoid expensive findViewById calls
     * during scrolling.
     *
     * @param itemView The root view of the recipe list item layout
     */
    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Cache references to views for efficient access
        private val titleView: TextView = itemView.findViewById(R.id.recipeTitle)
        private val descView: TextView = itemView.findViewById(R.id.recipeDescription)

        /**
         * Binds a RecipeEntity to the views in this ViewHolder.
         *
         * Updates the UI components with recipe data and sets up click handling.
         * This method is called every time this ViewHolder is recycled to display
         * a different recipe.
         *
         * @param recipe The RecipeEntity containing data to display
         */
        fun bind(recipe: RecipeEntity) {
            // Update text views with recipe data
            titleView.text = recipe.title
            descView.text = recipe.description

            // Set up click listener to handle recipe selection
            itemView.setOnClickListener {
                onItemClick(recipe)
            }
        }
    }
}