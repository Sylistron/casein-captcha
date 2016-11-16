package org.dasein.captcha;

import java.io.Serializable;
import java.util.Date;

import org.dasein.persist.PersistenceException;
import org.dasein.util.CachedItem;

public class Response implements CachedItem, Serializable {
	
	private static final long serialVersionUID = -5598550341289385949L;
	
	public static final String RESPONSE_ID = "responseId";
	public static final String CHALLENGE = "challenge";
	
	private Number responseId = null;
	private Boolean active = null;
	private String answer = null;
	private Number challenge = null;
	private Number created = null;
	private String createdBy = null;
	private Number lastModified = null;
	private String lastModifiedBy = null;
	
	public long getResponseId() {
		return responseId.longValue();
	}
	
	public boolean isActive() {
		return active;
	}	
	
	public String getAnswer() {
		return answer;
	}
	
	public long getCreated() {
		return created.longValue();
	}
	
	public Date getCreatedAsDate() {
		return new Date(created.longValue());
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	
	public long getLastModified() {
		return lastModified.longValue();
	}
	
	public Date getLastModifiedAsDate() {
		return new Date(lastModified.longValue());
	}
	
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	
	public Challenge getChallenge() throws PersistenceException {
		return CaptchaFactory.getInstance().getChallenge(challenge.longValue());
	}
	
	private transient long nextSync = -1L;
	
	public void invalidate() {
        nextSync = 3000L + System.currentTimeMillis();
    }
	
	public boolean isValidForCache() {
        if( nextSync == -1L ) {
            nextSync = System.currentTimeMillis() + 300000L;
        }
        return (System.currentTimeMillis() < nextSync);
    }   

}
