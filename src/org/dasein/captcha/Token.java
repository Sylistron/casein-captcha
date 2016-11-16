package org.dasein.captcha;

public class Token implements Comparable<Token> {
	private String tokenKey;
	private Challenge challenge;
	private long used;
	
	public Token(String tokenKey, Challenge challenge) {
		super();
		this.tokenKey = tokenKey;
		this.challenge = challenge;
		used = System.currentTimeMillis();
	}

	public String getTokenKey() {
		return tokenKey;
	}
	
	public Challenge getChallenge() {
		return challenge;
	}

	public long getUsed() {
		return used;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((tokenKey == null) ? 0 : tokenKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		if (tokenKey == null) {
			if (other.tokenKey != null)
				return false;
		} else if (!tokenKey.equals(other.tokenKey))
			return false;
		return true;
	}

	public int compareTo(Token t) {
		return new Long(t.getUsed()).compareTo(used); // reverse (descending) order
	}		
}