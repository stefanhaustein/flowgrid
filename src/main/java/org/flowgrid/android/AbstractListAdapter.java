package org.flowgrid.android;

import java.util.HashSet;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public abstract class AbstractListAdapter<T> implements ListAdapter {

  HashSet<DataSetObserver> observers = new HashSet<DataSetObserver>();
  
  @Override
  public abstract T getItem(int position);

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public int getItemViewType(int position) {
    return 0;
  }


  @Override
  public int getViewTypeCount() {
    return 1;
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public boolean isEmpty() {
    return getCount() == 0;
  }

  @Override
  public void registerDataSetObserver(DataSetObserver observer) {
    observers.add(observer);
  }

  @Override
  public void unregisterDataSetObserver(DataSetObserver observer) {
    observers.remove(observer);
  }

  @Override
  public boolean areAllItemsEnabled() {
    return true;
  }

  @Override
  public boolean isEnabled(int position) {
    return true;
  }

  public void notifyDataSetChanged() {
    for (DataSetObserver observer: observers) {
      observer.onChanged();
    }
  }

  protected static View createViewFromResource(Context context, int resourceId, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    return inflater.inflate(resourceId, parent, false);
  }
  
}
