package de.luhmer.owncloudnewsreader.helper;

import android.os.Parcel;
import android.os.Parcelable;

public class AidlException implements Parcelable {
	private Exception mException;
	
	public static final Parcelable.Creator<AidlException> CREATOR = new Parcelable.Creator<AidlException>() {
        public AidlException createFromParcel(Parcel source) {
            final AidlException f = new AidlException();
            f.mException = (Exception) source.readValue(AidlException.class.getClassLoader());
            return f;
        }

        public AidlException[] newArray(int size) {
            throw new UnsupportedOperationException();
        }

    };
    
    public AidlException() {	
	}
    
    public AidlException(Exception ex) {
    	this.mException = ex;
	}
	
	@Override
	public int describeContents() {		
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {		
		dest.writeValue(mException);
	}

	/**
	 * @return the mException
	 */
	public Exception getmException() {
		return mException;
	}
}
