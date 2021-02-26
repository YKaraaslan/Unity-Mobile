package com.unity.ProductsClient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.unity.R;

import java.util.List;

public class WebGuideAdapter extends RecyclerView.Adapter<WebGuideAdapter.RecyclerViewHolder> {
    List<WebGuideItems> webGuideItemsList;
    Context context;
    WebGuideCallBack callBack;

    public WebGuideAdapter(Context context, List<WebGuideItems> webGuideItemsList, WebGuideCallBack callBack){
        this.webGuideItemsList = webGuideItemsList;
        this.context = context;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_web_guide_recyclerview, parent, false);
        return new WebGuideAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.image.setImageResource(webGuideItemsList.get(position).getThumbnail());
        holder.title.setText(webGuideItemsList.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return webGuideItemsList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        CardView cardView;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.title);
            cardView = itemView.findViewById(R.id.cardView);

            cardView.setOnClickListener(view -> callBack.onItemClick(getAdapterPosition(), image, title));
        }
    }
}
