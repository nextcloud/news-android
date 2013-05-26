package de.luhmer.owncloudnewsreader.data;

public class FolderSubscribtionItem extends AbstractItem {

	public String headerFolder;
    public String idFolder;

	public FolderSubscribtionItem(String headerFolder, String idFolder, long idFolder_database) {
        super(idFolder_database);

		this.headerFolder = headerFolder;
		this.idFolder = idFolder;
	}
}
