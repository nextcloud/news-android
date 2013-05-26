package de.luhmer.owncloudnewsreader.data;

public class ConcreteSubscribtionItem {

	public String folder_id;
	public String subscription_id;
    public String favIcon;
    public String header;
	public long id_database;

	public ConcreteSubscribtionItem(String header, String folder_id, String subscription_id, String favIcon, long id_database/*, String parent_title*/) {

        this.header = header;
		this.folder_id = folder_id;
		this.subscription_id = subscription_id;
        this.favIcon = favIcon;
        this.id_database = id_database;
	}	
}
