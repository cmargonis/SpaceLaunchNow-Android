package me.calebjones.spacelaunchnow.ui.launches.agency;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.kinst.jakub.view.SimpleStatefulLayout;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.common.BaseFragment;
import me.calebjones.spacelaunchnow.common.customviews.SimpleDividerItemDecoration;
import me.calebjones.spacelaunchnow.content.data.callbacks.Callbacks;
import me.calebjones.spacelaunchnow.content.data.previous.PreviousDataRepository;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.data.models.main.LaunchList;
import me.calebjones.spacelaunchnow.ui.main.launches.ListAdapter;
import me.calebjones.spacelaunchnow.utils.views.EndlessRecyclerViewScrollListener;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreviousAgencyLaunchesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreviousAgencyLaunchesFragment extends BaseFragment {

    private static final String SEARCH_TERM = "searchTerm";
    private static final String LSP_NAME = "lspName";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.stateful_view)
    SimpleStatefulLayout statefulView;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    private LinearLayoutManager linearLayoutManager;
    private ListAdapter adapter;
    private String searchTerm = null;
    private String lspName = null;
    private PreviousDataRepository previousDataRepository;
    private int nextOffset = 0;
    private EndlessRecyclerViewScrollListener scrollListener;
    public boolean canLoadMore;
    private boolean statefulStateContentShow = false;
    private Context context;

    private OnFragmentInteractionListener mListener;
    private Unbinder unbinder;

    public PreviousAgencyLaunchesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param searchTerm Parameter 1.
     * @param lspName    Parameter 2.
     * @return A new instance of fragment PreviousLauncherLaunchesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PreviousAgencyLaunchesFragment newInstance(String searchTerm, String lspName) {
        PreviousAgencyLaunchesFragment fragment = new PreviousAgencyLaunchesFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_TERM, searchTerm);
        args.putString(LSP_NAME, lspName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchTerm = getArguments().getString(SEARCH_TERM);
            lspName = getArguments().getString(LSP_NAME);
        }
        context = getActivity();
        previousDataRepository = new PreviousDataRepository(context, getRealm());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_launch_list, container, false);
        unbinder = ButterKnife.bind(this, view);

        adapter = new ListAdapter(context);
        linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        recyclerView.setAdapter(adapter);
        statefulView.showProgress();
        statefulView.setOfflineRetryOnClickListener(v -> fetchData(true));
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                if (canLoadMore) {
                    fetchData(false);
                    mListener.showPreviousLoading(true);

                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        fetchData(true);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.v("onDestroyView");
        unbinder.unbind();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void fetchData(boolean forceRefresh) {
        Timber.v("Sending GET_UP_LAUNCHES");
        if (forceRefresh) {
            nextOffset = 0;
            adapter.clear();
        }

        previousDataRepository.getPreviousLaunches(nextOffset, searchTerm, lspName, null, null, new Callbacks.ListCallbackMini() {
            @Override
            public void onLaunchesLoaded(List<LaunchList> launches, int next, int total) {
                Timber.v("Offset - %s", next);
                nextOffset = next;
                canLoadMore = next > 0;
                updateAdapter(launches);
                mListener.setPreviousBadge(total);
            }

            @Override
            public void onNetworkStateChanged(boolean refreshing) {
//                showNetworkLoading(refreshing);
                mListener.showPreviousLoading(refreshing);
            }

            @Override
            public void onError(String message, @Nullable Throwable throwable) {
                statefulView.showOffline();
                statefulStateContentShow = false;
                if (throwable != null) {
                    Timber.e(throwable);
                } else {
                    Timber.e(message);
                }
            }
        });
    }

    private void updateAdapter(List<LaunchList> launches) {

        if (launches.size() > 0) {
            if (!statefulStateContentShow) {
                statefulView.showContent();
                statefulStateContentShow = true;
            }
            adapter.addItems(launches);
            adapter.notifyDataSetChanged();

        } else {
            statefulView.showEmpty();
            statefulStateContentShow = false;
            if (adapter != null) {
                adapter.clear();
            }
        }
        scrollListener.resetState();
    }

    public void onRefresh(String lspName, String searchTerm) {
        this.searchTerm = searchTerm;
        this.lspName = lspName;
        fetchData(true);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void showPreviousLoading(boolean loading);

        void setPreviousBadge(int count);

    }
}
