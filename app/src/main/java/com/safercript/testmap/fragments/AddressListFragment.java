package com.safercript.testmap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.safercript.testmap.MapsActivity;
import com.safercript.testmap.R;
import com.safercript.testmap.adapters.MyItemRecyclerViewAdapter;
import com.safercript.testmap.entity.LatLngAddress;

public class AddressListFragment extends Fragment {

    private static final String ARG_KEY = "key";

    private OnListFragmentInteractionListener mListener;
    private int keyAddress;

    public AddressListFragment() {
    }

    @SuppressWarnings("unused")
    public static AddressListFragment newInstance(int keyAddress) {
        AddressListFragment fragment = new AddressListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_KEY, keyAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            keyAddress = getArguments().getInt(ARG_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(((MapsActivity)getActivity()).getLatLngLngAddresses(), mListener, keyAddress));
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {

        void onListFrom(LatLngAddress item);

        void onListTo(LatLngAddress item);
    }
}
