package de.luhmer.owncloudnewsreader.ListView;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.luhmer.owncloudnewsreader.R;
import de.luhmer.owncloudnewsreader.data.ConcreteSubscribtionItem;

public class NewsReader_CLI implements NewsReader_Row {
    final ConcreteSubscribtionItem item;
    final LayoutInflater inflater;
    Integer percentage = 0;
	
	public NewsReader_CLI(ConcreteSubscribtionItem item, LayoutInflater inflater)
	{
		this.item = item;
		this.inflater = inflater;	
		
	}
	
	public View getView(View convertView) {	
		ViewHolder holder;		
        View view;
        if (convertView == null) {  
            ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.subscription_detail_list_item, null);            
            TextView tv = (TextView)viewGroup.findViewById(R.id.summary);
            TextView tv2 = (TextView)viewGroup.findViewById(R.id.tv_subscription);
            holder = new ViewHolder(tv, tv2); 
            
            
            //holder.cb_Checked.setTag(vtoDo.get_uid());
            //Log.d("TASK_SYNC", "HOLDER Checkbox Tag: " + holder.cb_Checked.getTag());
            
            viewGroup.setTag(holder);
        	
            view = viewGroup;
        } else {
            view = convertView;
            holder = (ViewHolder)convertView.getTag();
        }
                
        
        holder.txtSummary.setText(item.header);
        //holder.txtSummary.setText(item.parent_title);
        
        if(percentage >= 100)
		{
			holder.txtSummary.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		}
        else
        {			
        	holder.txtSummary.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
        }
        return view;
    }

    public int getViewType() {
        return RowTypes.RowType.NewsReader_CLI.ordinal();
    }
       
    
    private static class ViewHolder {
    	protected TextView txtSummary;  
    	@SuppressWarnings("unused")
		protected TextView txtSubscription;
    	
        private ViewHolder(TextView txtSummary, TextView txtSubscription) {
            this.txtSummary = txtSummary;
            this.txtSubscription = txtSubscription;
        }
    }


	@Override
	public ConcreteSubscribtionItem getSubscItem() {		
		return item;
	}
}
