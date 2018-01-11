package protochat;

import java.util.List;
import java.util.ArrayList;

public class MessageList {

	private static final int DEFAULT_MAX_CAP = 1024;

	private final int cap;
	private final List<Message> msgs;
	private int nClients;
	private int fstMsg, nextMsg;

	public MessageList() { this(DEFAULT_MAX_CAP); }

	public MessageList(int cap) {
		if (cap <= 0) {
			throw new IllegalArgumentException("Illegal capacity: " + cap);
		}
		this.cap = cap;
		msgs = new ArrayList<>(cap);
		nClients = 0;
		fstMsg = nextMsg = 0;
	}

	public synchronized int newClient() {
		++nClients;
		return nextMsg;
	}

	public synchronized void add(String msg) {
		if ((nextMsg - fstMsg) == cap) {
			msgs.remove(0);
			++fstMsg;
		}
		msgs.add(new Message(msg, nClients));
		++nextMsg;
		notifyAll();
	}

	public synchronized String get(int msgNo) throws InterruptedException {
		int i = msgNo - fstMsg;

		if (i < 0)
			return null;

		// Wait while there are no new messages
		while (msgNo == nextMsg) {
			wait();
		}
		i = msgNo - fstMsg; // update i because fstMsg may have changed
		Message m = msgs.get(i);

		--m.nReadsLeft;
		if (m.nReadsLeft == 0) {
			msgs.remove(i);
			++fstMsg;
		}
		return m.txt;
	}

	public synchronized void leave(int msgNo) {
		--nClients;

		if (msgNo < fstMsg) // can happen if the client is slow
			msgNo = fstMsg;

		int nMsgs = msgs.size();
		for (int i = msgNo - fstMsg; i < nMsgs; ++i) {
			Message m = msgs.get(i);

			--m.nReadsLeft;
			if (m.nReadsLeft == 0) {
				msgs.remove(i);
				if (i == fstMsg) {
					++fstMsg;
				}
			}
		}
	}

	private static class Message {

		public final String txt;
		public int nReadsLeft;

		private Message() { txt = null; }

		public Message(String txt, int nReadsLeft) {
			this.txt = txt;
			this.nReadsLeft = nReadsLeft;
		}
	}
}
