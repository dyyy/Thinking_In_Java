package yuanye.datastructure.btree;

/**
 * Created by Kali on 14-5-26.
 */
public class BTree<K extends Comparable<K>> {
    private final int degree;
    private AbstractBTreeNode<K> root;

    public BTree(int degree){
        if (degree < 2){
            throw new IllegalArgumentException("degree mustn't < 2");
        }
        this.degree = degree;
        root = new BTreeLeaf<>(degree);
    }

    public void insert(K key){
        AbstractBTreeNode<K> n = root;
        if (root.isFull()){
            AbstractBTreeNode<K> newRoot = new BTreeInternalNode<>(degree);
            newRoot.insertChild(n,0);
            newRoot.splitChild(0);
            n = newRoot;
            root = newRoot;
        }
        n.insertNotFull(key);
    }

    public void delete(K key){
        AbstractBTreeNode<K> node = root;
        node.deleteNotEmpty(key);
        if (node.nkey() == 0){
            //shrink
            root = node.getChild(0);
        }
    }

    @Override
    public String toString() {
       return AbstractBTreeNode.BTreeToString(this.root);
    }

    public static void main(String[] args) {
        BTree<Integer> bTree = new BTree(2);
        int[] numbers = new int[]{6,18,16,22,3,12,8,10,20,21,13,17};
        for(int i = 0; i < numbers.length; i++){
            bTree.insert(numbers[i]);
        }
        System.out.println(bTree);
        bTree.delete(20);
        System.out.println(bTree);
    }

}
