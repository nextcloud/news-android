/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.owncloudnewsreader.model;

import java.util.Date;
import java.util.List;

public class RssFile {
	private long DB_Id;
    private String Item_Id;
	private String Title;
	private String Link;
	private String Description;
	private Boolean Read;
	private Boolean Starred;
	private String Author;


	//private String StreamID;
	private String FeedID;
    private String FeedID_Db;
	private List<String> Categories;
	private Date date;
    private String guid;
    private String guidHash;
    private String lastModified;

    private String enclosureMime;
    private String enclosureLink;



    public RssFile(long DB_Id, String Item_Id, String Title, String Link, String Description, Boolean Read, String FeedID_Db, String FeedID, List<String> Categories, /*String StreamID, */Date date, Boolean Starred, String guid, String guidHash, String lastModified, String author, String enclosureLink, String enclosureMime) {
		this.setDB_Id(DB_Id);
		this.setTitle(Title);
		this.setLink(Link);
		this.setDescription(Description);
		this.setRead(Read);
		this.setStarred(Starred);
		this.setDate(date);
		this.setFeedID(FeedID);
		this.setCategories(Categories);
        this.setGuid(guid);
        this.setGuidHash(guidHash);
        this.setItem_Id(Item_Id);
        this.setFeedID_Db(FeedID_Db);
        this.setLastModified(lastModified);
        this.setAuthor(author);
        this.setEnclosureLink(enclosureLink);
        this.setEnclosureMime(enclosureMime);
	}

	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getLink() {
		return Link;
	}
	public void setLink(String link) {
		Link = link;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public Boolean getRead() {
		return Read;
	}
	public void setRead(Boolean read) {
		Read = read;
	}


    public void setEnclosureMime(String enclosureMime) {
        this.enclosureMime = enclosureMime;
    }

    public void setEnclosureLink(String enclosureLink) {
        this.enclosureLink = enclosureLink;
    }

    public String getEnclosureLink() {
        return enclosureLink;
    }

    public String getEnclosureMime() {
        return enclosureMime;
    }

	public String getFeedID() {
		return FeedID;
	}

	public void setFeedID(String feedID) {
		FeedID = feedID;
	}

	public long getDB_Id() {
		return DB_Id;
	}

	public void setDB_Id(long dB_Id) {
		DB_Id = dB_Id;
	}

	public List<String> getCategories() {
		return Categories;
	}

	public void setCategories(List<String> categories) {
		Categories = categories;
	}
/*
	public String getStreamID() {
		return StreamID;
	}

	public void setStreamID(String streamID) {
		StreamID = streamID;
	}
*/
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Boolean getStarred() {
		return Starred;
	}

	public void setStarred(Boolean starred) {
		Starred = starred;
	}

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getGuidHash() {
        return guidHash;
    }

    public void setGuidHash(String guidHash) {
        this.guidHash = guidHash;
    }

    public String getFeedID_Db() {
        return FeedID_Db;
    }

    public void setFeedID_Db(String feedID_Db) {
        FeedID_Db = feedID_Db;
    }

    public String getItem_Id() {
        return Item_Id;
    }

    public void setItem_Id(String item_Id) {
        Item_Id = item_Id;
    }

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return Author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		Author = author;
	}
}
