package com.pepster;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.MapView;
import com.pepster.data.MapContent;
import com.pepster.utilities.Sorter;
import com.pepster.views.MapViewHolder;

import java.util.HashSet;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public class MapRecyclerViewAdapter extends RecyclerView.Adapter<MapViewHolder> {

    private static final String TAG = MapRecyclerViewAdapter.class.getSimpleName();
    protected final HashSet<MapView> mMapViews = new HashSet<>();
    protected final SortedList<MapContent> mSortedList;
    private final Sorter<MapContent> mSorter;

    public MapRecyclerViewAdapter(Sorter<MapContent> sorter){
        mSorter = sorter;
        mSortedList = new SortedList<>(MapContent.class, new SortedListAdapterCallback<MapContent>(this) {
            @Override
            public int compare(MapContent t0, MapContent t1) {
                return mSorter.getComparator().compare(t0,t1);
            }
            @Override
            public boolean areContentsTheSame(MapContent oldItem, MapContent newItem) {
                if(newItem.getModTime()<oldItem.getModTime())//ensure that new item is not actually older and does not replace oldItem
                    throw new Error("new item actually older than old item");
                return oldItem.equals(newItem);
            }
            @Override
            public boolean areItemsTheSame(MapContent item1, MapContent item2) {
                return item1.ID().equals(item2.ID());
            }
        });
    }

    public int add(MapContent newItem) {
        return mSortedList.add(newItem);
    }

    public MapContent get(Func1<MapContent, Boolean> predicate){
        try {
            return Observable.range(0, mSortedList.size())
                    .firstOrDefault(null, i -> predicate.call(mSortedList.get(i)))
                    .map(i -> mSortedList.get(i))
                    .toBlocking()
                    .single();
        }catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    public MapContent remove(MapContent mapContent){
            return Observable.range(0, mSortedList.size())
                    .first(i -> mSortedList.get(i)==mapContent)
                    .map(i -> mSortedList.removeItemAt(i))
                    .toBlocking()
                    .single();
    }

    @Override
    public MapViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_listitem, viewGroup, false);
        final MapViewHolder viewHolder = new MapViewHolder(viewGroup.getContext(), view);
        mMapViews.add(viewHolder.mMapView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MapViewHolder viewHolder, int position) {
        MapContent content = mSortedList.get(position);
        //Kiinnitetään MapContent vieviin. Kun lista item karttaa klikataan heataan content getTag() metodilla MainAcitivityssa metodi onMapListItemClick(View v)
        viewHolder.itemView.setTag(content);
        viewHolder.setContent(content);
    }

    public Observable<MapContent> currentContentObservable(){
        Subject<MapContent, MapContent> contens = ReplaySubject.create();
        for (int i = 0; i < mSortedList.size(); i++) {
            contens.onNext(mSortedList.get(i));
        }
        contens.onCompleted();
        return contens.asObservable();
    }

    @Override
    public int getItemCount() {
        return mSortedList == null ? 0 : mSortedList.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }
}
