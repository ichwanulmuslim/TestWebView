package com.app.webdroid.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.webdroid.R;
import com.app.webdroid.activities.MainActivity;
import com.app.webdroid.databases.prefs.SharedPref;
import com.app.webdroid.models.Navigation;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class AdapterNavigation extends RecyclerView.Adapter<AdapterNavigation.ViewHolder> {

    private List<Navigation> items;
    Context context;
    private OnItemClickListener mOnItemClickListener;
    private int clickedItemPosition = -1;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Navigation obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterNavigation(Context context, List<Navigation> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView menuName;
        public ImageView menuIcon;
        public LinearLayout lytItem;
        public RelativeLayout lytParent;

        public ViewHolder(View v) {
            super(v);
            menuName = v.findViewById(R.id.menu_name);
            menuIcon = v.findViewById(R.id.menu_icon);
            lytItem = v.findViewById(R.id.lyt_item);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawer, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"RecyclerView", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Navigation obj = items.get(position);

        holder.menuName.setText(obj.name);
        Glide.with(context)
                .load(obj.icon.replace(" ", "%20"))
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_home)
                .centerCrop()
                .into(holder.menuIcon);

        if (sharedPref.getIsDarkTheme()) {
            holder.lytParent.setBackgroundColor(context.getResources().getColor(R.color.colorBackgroundDark));
        } else {
            holder.lytParent.setBackgroundColor(context.getResources().getColor(R.color.colorBackgroundLight));
        }

        holder.lytParent.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, obj, position);
                clickedItemPosition = position;
                notifyDataSetChanged();
                if (position == sharedPref.getLastItemPosition()) {
                    Log.d("Drawer", "item already selected");
                } else {
                    sharedPref.setLastItemPosition(position);
                    ((MainActivity) context).loadWebPage(obj.name, obj.type, obj.url);
                }
            }
        });

        if (clickedItemPosition == position) {
            holder.lytItem.setBackgroundResource(R.drawable.bg_selected_item);
            holder.menuName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.menuIcon.setColorFilter(context.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        } else {
            holder.lytItem.setBackgroundResource(R.drawable.bg_unselected_item);
            if (sharedPref.getIsDarkTheme()) {
                holder.menuName.setTextColor(ContextCompat.getColor(context, R.color.colorTextDarkNavigation));
                holder.menuIcon.setColorFilter(context.getResources().getColor(R.color.colorTextDarkNavigation), PorterDuff.Mode.SRC_IN);
            } else {
                holder.menuName.setTextColor(ContextCompat.getColor(context, R.color.colorTextDefault));
                holder.menuIcon.setColorFilter(context.getResources().getColor(R.color.colorTextDefault), PorterDuff.Mode.SRC_IN);
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void setListData(List<Navigation> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}