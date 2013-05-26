package de.luhmer.owncloudnewsreader.ListView;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloudnewsreader.data.ConcreteSubscribtionItem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class Subscription_ListViewAdapter extends BaseAdapter {
	List<Subscription_Row> rows;		
	LayoutInflater inflator;
	//ToDoList toDoList; 
	
	public Subscription_ListViewAdapter(Context context) {
		super();
		rows = new ArrayList<Subscription_Row>();
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//this.toDoList = tdl;
	}
		
	@Override
    public int getViewTypeCount() {
        return RowTypes.RowType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).getViewType();
    }
	
	@Override
	public int getCount() {
		return rows.size();
	}

	@Override
	public Object getItem(int index) {		
		return rows.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;		
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		try
		{
			View v = rows.get(index).getView(view); 
			return v;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();			
		}
		return null;
	}
	
	/*
	public boolean removeItem(String uid)
	{
		for (int i = 0 ; i < subscriptionItems.size(); i++) {
			if(subscriptionItems.get(i).getvTodo().get_uid().equals(uid))
			{
				subscriptionItems.remove(i);
				break;
			}
		}
		this.notifyDataSetChanged();
		return true;
	}
	*/
	/*
	public Integer getIdOfTodo(String uid)	
	{
		for (int i = 0 ; i < subscriptionItems.size(); i++) {
			if(subscriptionItems.get(i).getSubscItem())			
				return i;
		}
		
		return -1;
	}*/
	
	public void ReplaceRow(ConcreteSubscribtionItem vTodo, int position)
	{
		rows.remove(position);
		rows.add(position, new Subscription_CLI(vTodo, inflator));
		this.notifyDataSetChanged();
	}
		
	public ArrayList<Subscription_Row> convertItems(ArrayList<ConcreteSubscribtionItem> items, Activity activity)
	{
		ArrayList<Subscription_Row> temp = new ArrayList<Subscription_Row>();
		
		for (ConcreteSubscribtionItem vtodo : items) {			
			temp.add(new Subscription_CLI(vtodo, inflator));
		}
		
		return temp;
	}
	
	public void updateEntries(ArrayList<ConcreteSubscribtionItem> item, Activity activity)
	{
		rows = new ArrayList<Subscription_Row>();
		rows.addAll(convertItems(item, activity));		
		this.notifyDataSetChanged();
	}
}