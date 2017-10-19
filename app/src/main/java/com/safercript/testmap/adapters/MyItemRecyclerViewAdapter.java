package com.safercript.testmap.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.safercript.testmap.fragments.AddressListFragment.OnListFragmentInteractionListener;
import com.safercript.testmap.R;
import com.safercript.testmap.entity.LatLngAddress;

import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<LatLngAddress> mValues;
    private final OnListFragmentInteractionListener mListener;
    private int mKeyAddress;

    public MyItemRecyclerViewAdapter(List<LatLngAddress> items, OnListFragmentInteractionListener listener, int keyAddress) {
        mValues = items;
        mListener = listener;
        mKeyAddress = keyAddress;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_address_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).getFullAddress());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    if (mKeyAddress == 0){
                        mListener.onListFrom(holder.mItem);
                    }
                    if (mKeyAddress == 1){
                        mListener.onListTo(holder.mItem);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public LatLngAddress mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
