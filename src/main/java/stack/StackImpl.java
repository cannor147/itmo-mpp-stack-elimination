package stack;

import kotlinx.atomicfu.AtomicIntArray;
import kotlinx.atomicfu.AtomicRef;

import java.util.concurrent.ThreadLocalRandom;

public class StackImpl implements Stack {
    private static final int ELIMINATION_ITERATING = 2;
    private static final int ELIMINATION_SIZE = 8;

    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    private final int[] eliminationArray = new int[ELIMINATION_SIZE];
    private final AtomicIntArray eliminationStates = new AtomicIntArray(ELIMINATION_SIZE);

    // head pointer
    private AtomicRef<Node> head = new AtomicRef<>(null);

    @Override
    public void push(int x) {
        int eliminationIndex = -1;
        int y = ThreadLocalRandom.current().nextInt(ELIMINATION_SIZE);
        for (int i = 0; i < ELIMINATION_ITERATING; i++, y++) {
            if (y == ELIMINATION_SIZE) {
                y = 0;
            }

            if (eliminationStates.get(y).compareAndSet(0, 5)) {
                eliminationArray[y] = x;
                eliminationStates.get(y).setValue(1);
                eliminationIndex = y;
                break;
            }
        }

        while (true) {
            Node curHead = head.getValue();
            Node futureHead = new Node(x, curHead);
            if (eliminationIndex >= 0) {
                if (eliminationStates.get(eliminationIndex).compareAndSet(1, 2)) {
                    if (head.compareAndSet(curHead, futureHead)) {
                        eliminationStates.get(eliminationIndex).setValue(0);
                        return;
                    } else {
                        eliminationStates.get(eliminationIndex).setValue(1);
                    }
                } else if (eliminationStates.get(eliminationIndex).getValue() == 4) {
                    return;
                }
            } else {
                if (head.compareAndSet(curHead, futureHead)) {
                    return;
                }
            }
        }
    }

    @Override
    public int pop() {
        int y = ThreadLocalRandom.current().nextInt(ELIMINATION_SIZE);
        for (int i = 0; i < ELIMINATION_ITERATING; i++, y++) {
            if (y == ELIMINATION_SIZE) {
                y = 0;
            }

            if (eliminationStates.get(y).compareAndSet(1, 3)) {
                int k = eliminationArray[y];
                eliminationStates.get(y).setValue(4);
                return k;
            }
        }

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
