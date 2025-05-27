import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reseptimasiina.R
import com.example.reseptimasiina.RecipeEntity

class RecipeAdapter(
    private val onItemClick: (RecipeEntity) -> Unit
) : ListAdapter<RecipeEntity, RecipeAdapter.RecipeViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecipeEntity>() {
            override fun areItemsTheSame(oldItem: RecipeEntity, newItem: RecipeEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: RecipeEntity, newItem: RecipeEntity) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        holder.bind(recipe)
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.recipeTitle)
        private val descView: TextView = itemView.findViewById(R.id.recipeDescription)

        fun bind(recipe: RecipeEntity) {
            titleView.text = recipe.title
            descView.text = recipe.description

            itemView.setOnClickListener {
                onItemClick(recipe)
            }
        }
    }
}
