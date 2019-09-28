package stack;

import kotlinx.atomicfu.AtomicRef;

public class StackImpl implements Stack {
    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    // head pointer
    private AtomicRef<Node> head = new AtomicRef<>(null);

    @Override
    public void push(int x) {
        while (true) {
            Node curHead = head.getValue();
            Node futureHead = new Node(x, curHead);
            if (head.compareAndSet(curHead, futureHead)) {
                return;
            }
        }
    }

    @Override
    public int pop() {
        while (true) {
            Node curHead = head.getValue();
            if (curHead == null) {
                return Integer.MIN_VALUE;
            }

            Node futureHead = curHead.next.getValue();
            if (head.compareAndSet(curHead, futureHead)) {
                return curHead.x;
            }
        }
    }
}
