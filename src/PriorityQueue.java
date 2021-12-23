public class PriorityQueue{

    private MessageNode head;
    private int size;

    public PriorityQueue(){
        head = null;
        size = 0;
    }

    /**
     * After being checked, message is being placed in proper location.
     * @param msg: Message to be added to the Queue
     */
    public synchronized void add(Message msg){
        if (size == 0){
            head = new MessageNode();
            head.current = msg;
            size++;
        }
        else{
            MessageNode traverser = head, beforeTraverser;
            boolean added = false;
            do{
                int result = traverser.current.compareTo(msg); // compare messages' sending time.
                if (result > 0){ // msg should be placed before current message.
                    MessageNode newMessage = new MessageNode(); // create new node
                    newMessage.current = msg; // add content to that node
                    // handle node connections
                    newMessage.previous = traverser.previous;
                    newMessage.next = traverser;
                    traverser.previous = newMessage;
                    if(newMessage.previous != null) // previous field of the first position will be null. need to be handled.
                        newMessage.previous.next = newMessage;
                    else{ // we are replacing first element in the queue. Head should be moved as well.
                        head = newMessage;
                    }
                    added = true;
                    size++;
                }
                beforeTraverser = traverser;
                traverser = traverser.next;
            }while(traverser != null);

            if (!added){ // our message is the latest message. add to last
                MessageNode message = new MessageNode();
                message.current = msg;
                message.previous = beforeTraverser;
                beforeTraverser.next = message;
                size++;
            }
        }
    }

    public int getSize() {
        return size;
    }

    public void traverse(){
        MessageNode current = head;

        while(current != null){
            System.out.println(current.current);
            current = current.next;
        }
        System.out.println(size);
    }

    /**
     * @return : Returns the first element of the Queue. If queue is empty, returns null.
     */
    public Message get(){
        return head.current;
    }

    public void remove(){
        if (size == 0){ // there is nothing to remove.
            return;
        }
        head = head.next;
        size--;
        if (head != null){ // new head position is not null
            head.previous = null;
        }
    }

    // this class is used to hold position of the Message in the queue.
    private class MessageNode{

        public MessageNode previous; // reference to the previous message
        public Message current;
        public MessageNode next; // reference to the next message

        public MessageNode(){
        }
    }
}
