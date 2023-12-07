/*
PeopleRecord Class: Comparable Interface
Your PeopleRecord class has a compareTo method, but it does not implement the Comparable interface. To adhere to Java's conventions and ensure compatibility with collections that rely on comparison (like a sorted tree or heap), it's recommended to explicitly implement Comparable<PeopleRecord>.

MyBST Class: Handling Duplicates
TODO:Your current MyBST implementation does not account for the possibility of duplicate entries. Since you're comparing records by family name, consider what should happen if two records have the same family name. You might need to define a secondary comparison criterion or handle duplicates in some other manner.

MyHeap Class: Comparison Logic
TODO:Ensure that the comparison logic in your MyHeap class aligns with how you want to structure the heap (min-heap or max-heap). The current implementation seems to be for a min-heap. If a max-heap is desired, you'll need to adjust the comparison logic in heapifyUp and heapifyDown.

MyHashmap Class: Resize Logic and Deleted Entries
Your MyHashmap does not currently handle resizing when it becomes full, nor does it have a mechanism to handle deleted entries (like using a special object to mark deleted slots). Depending on the expected use case and data size, you might want to implement these features.
TODO:Additionally, ensure that the hash function (getHashIndex) evenly distributes entries to minimize collisions.

General:
Error Handling
TODO:Consider adding error handling, particularly for edge cases. For example, what should happen if a null PeopleRecord is inserted into the BST or Heap? Should your Hashmap throw an exception when it's full, or should it resize?

Testing and Validation
TODO:Thoroughly test each class to ensure they behave as expected, particularly under edge cases (like inserting duplicate values into the BST or handling collisions in the Hashmap).

DatabaseProcessing Class
TODO:The DatabaseProcessing class is currently empty. This class should eventually include the logic for interacting with these data structures and implementing the required functionalities like loading data, searching, sorting, etc.
 */

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.stream.Collectors;

public class DatabaseProcessing {
    private MyBST bst;
    private MyHeap heap;
    private MyHashmap hashmap;

    public DatabaseProcessing() {
        bst = new MyBST();
        heap = new MyHeap();
        hashmap = new MyHashmap(100); // Assuming an initial capacity of 100
    }

    // Method a: loadData
    public void loadData(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] data = line.split(";");
            PeopleRecord record = new PeopleRecord(
                    data[0], data[1], data[2], data[3], data[4], data[5],
                    data[6], data[7], data[8], data[9], data[10], data[11], data[12]
            );
            bst.insert(record);
        }
        scanner.close();
    }

    // Method b: search
    public List<PeopleRecord> search(String givenName, String familyName) {
        return bst.search(givenName, familyName);
    }

    // Method c: sort
    public List<PeopleRecord> sort() {
        List<PeopleRecord> sortedList = new ArrayList<>();
        transferBSTtoHeap(bst.root);
        while (heap.size() > 0) {
            sortedList.add(heap.remove());
        }
        return sortedList;
    }

    private void transferBSTtoHeap(PeopleRecord node) {
        if (node != null) {
            heap.insert(node);
            transferBSTtoHeap(node.left);
            transferBSTtoHeap(node.right);
        }
    }

    // Method d: getMostFrequentWords
    public Map<String, Integer> getMostFrequentWords(String fileName, int count, int len) throws FileNotFoundException, ShortLengthException {
        if (len < 3) {
            throw new ShortLengthException("Length is less than 3");
        }

        Scanner scanner = new Scanner(new File(fileName));
        Map<String, Integer> wordFrequencyMap = new HashMap<>();

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] words = line.split(";");

            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z]", "");
                if (word.length() >= len) {
                    wordFrequencyMap.put(word, wordFrequencyMap.getOrDefault(word, 0) + 1);
                }
            }
        }
        scanner.close();

        // Create a list from elements of the hashmap
        List<Map.Entry<String, Integer>> list = new ArrayList<>(wordFrequencyMap.entrySet());

        // Sort the list using lambda expression
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // Put the sorted data back into the hashmap
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        // Return only the top 'count' elements
        return sortedMap.entrySet().stream()
                .limit(count)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    // Custom exception class
    class ShortLengthException extends Exception {
        public ShortLengthException(String message) {
            super(message);
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        DatabaseProcessing dbProcessing = new DatabaseProcessing();
        try {
            dbProcessing.loadData("resources/people.txt");

            // Test search
            List<PeopleRecord> searchResults = dbProcessing.search("Johnetta", "Abdallah");
            System.out.println("Search Results: " + searchResults);

            // Test sort
            List<PeopleRecord> sortedRecords = dbProcessing.sort();
//            System.out.println("Sorted Records: " + sortedRecords);

            // Test getMostFrequentWords
            Map<String, Integer> frequentWords = dbProcessing.getMostFrequentWords("resources/people.txt", 5, 3);
            System.out.println("Frequent Words: " + frequentWords);

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (ShortLengthException e) {
            System.err.println("Short length exception: " + e.getMessage());
        }
    }
}


class MyBST {
    PeopleRecord root; // Root node of the BST

    // Constructor
    public MyBST() {
        this.root = null;
    }

    // Method to get information about the tree (total nodes and height)
    public String getInfo() {
        int totalNodes = countNodes(root);
        int height = treeHeight(root);
        return "Total nodes: " + totalNodes + ", Height of tree: " + height;
    }

    // Helper method to count nodes
    private int countNodes(PeopleRecord node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.left) + countNodes(node.right);
    }

    // Helper method to determine the height of the tree
    private int treeHeight(PeopleRecord node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(treeHeight(node.left), treeHeight(node.right));
    }

    // Method to insert a new PeopleRecord into the tree
    public void insert(PeopleRecord newRecord) {
        root = insertRecord(root, newRecord);
    }

    // Recursive helper method for insertion
    private PeopleRecord insertRecord(PeopleRecord current, PeopleRecord newRecord) {
        if (current == null) {
            return newRecord;
        }
        if (newRecord.compareTo(current) < 0) {
            current.left = insertRecord(current.left, newRecord);
        } else if (newRecord.compareTo(current) > 0) {
            current.right = insertRecord(current.right, newRecord);
        }
        return current; // Return the (unchanged) node pointer
    }

    // Method to search for records by first/given name and family name
    public List<PeopleRecord> search(String givenName, String familyName) {
        List<PeopleRecord> matchingRecords = new ArrayList<>();
        searchRecords(root, givenName, familyName, matchingRecords);
        return matchingRecords;
    }

    // Recursive helper method for search
    private void searchRecords(PeopleRecord node, String givenName,
                               String familyName, List<PeopleRecord> matchingRecords) {
        if (node != null) {
            if (node.getGivenName().equals(givenName) && node.getFamilyName().equals(familyName)) {
                matchingRecords.add(node);
            }
            // Assuming the tree is ordered by family name, we can decide which subtree to search
            if (familyName.compareTo(node.getFamilyName()) < 0) {
                searchRecords(node.left, givenName, familyName, matchingRecords);
            } else {
                searchRecords(node.right, givenName, familyName, matchingRecords);
            }
        }
    }
}

class MyHeap {
    private List<PeopleRecord> heap;

    // Constructor
    public MyHeap() {
        this.heap = new ArrayList<>();
    }

    // Method to add a new PeopleRecord into the heap
    public void insert(PeopleRecord newRecord) {
        heap.add(newRecord); // Add at the end of the list
        heapifyUp(heap.size() - 1); // Adjust the heap from the last element upwards
    }

    // Helper method to maintain the heap property from bottom to top
    private void heapifyUp(int index) {
        int parentIndex = (index - 1) / 2;
        if (index > 0 && heap.get(index).compareTo(heap.get(parentIndex)) < 0) {
            // Swap if the current node is smaller than its parent
            Collections.swap(heap, index, parentIndex);
            // Heapify up from the parent's position
            heapifyUp(parentIndex);
        }
    }

    // Method to remove the root element (minimum element) from the heap
    public PeopleRecord remove() {
        if (heap.isEmpty()) {
            return null; // Or throw an exception
        }
        PeopleRecord removedRecord = heap.get(0); // The root element
        heap.set(0, heap.get(heap.size() - 1)); // Move the last element to the root
        heap.remove(heap.size() - 1); // Remove the last element
        heapifyDown(0); // Adjust the heap from the root downwards
        return removedRecord;
    }

    // Method to get the size of the heap
    public int size() {
        return heap.size();
    }

    // Helper method to maintain the heap property from top to bottom
    private void heapifyDown(int index) {
        int smallest = index;
        int leftChildIndex = 2 * index + 1;
        int rightChildIndex = 2 * index + 2;

        if (leftChildIndex < heap.size() && heap.get(leftChildIndex).compareTo(heap.get(smallest)) < 0) {
            smallest = leftChildIndex;
        }

        if (rightChildIndex < heap.size() && heap.get(rightChildIndex).compareTo(heap.get(smallest)) < 0) {
            smallest = rightChildIndex;
        }

        if (smallest != index) {
            // Swap if a child node is smaller than the current node
            Collections.swap(heap, index, smallest);
            // Heapify down from the smallest child's position
            heapifyDown(smallest);
        }
    }
}

class MyHashmap {
    private PeopleRecord[] table;
    private int capacity;
    private int size;

    // Constructor
    public MyHashmap(int capacity) {
        this.capacity = capacity;
        this.table = new PeopleRecord[capacity];
        this.size = 0;
    }

    // Method to add a PeopleRecord to the hashmap
    public void put(PeopleRecord record) {
        int index = getHashIndex(record.getFamilyName());
        int originalIndex = index;
        int i = 1;

        while (table[index] != null) {
            index = (originalIndex + i * i) % capacity; // Quadratic probing
            if (index == originalIndex) { // Table is full
                return; // Or resize the table
            }
            i++;
        }

        table[index] = record;
        size++;
    }

    // Method to get a PeopleRecord by key (e.g., family name)
    public PeopleRecord get(String key) {
        int index = getHashIndex(key);
        int originalIndex = index;
        int i = 1;

        while (table[index] != null) {
            if (table[index].getFamilyName().equals(key)) {
                return table[index];
            }
            index = (originalIndex + i * i) % capacity; // Quadratic probing
            if (index == originalIndex) {
                break;
            }
            i++;
        }

        return null; // Not found
    }

    // Method to delete a PeopleRecord by key
    public void delete(String key) {
        int index = getHashIndex(key);
        int originalIndex = index;
        int i = 1;

        while (table[index] != null) {
            if (table[index].getFamilyName().equals(key)) {
                table[index] = null; // Mark as deleted
                size--;
                return;
            }
            index = (originalIndex + i * i) % capacity; // Quadratic probing
            if (index == originalIndex) {
                break;
            }
            i++;
        }
    }

    // Method to compute the hash index
    private int getHashIndex(String key) {
        return key.hashCode() % capacity;
    }

    // Method to check the number of records in the hashmap
    public int size() {
        return size;
    }

    // Additional methods as required...
}


class PeopleRecord implements Comparable<PeopleRecord>{
    // Attributes
    private String givenName;
    private String familyName;
    private String companyName;
    private String address;
    private String city;
    private String county;
    private String state;
    private String zip;
    private String phone1;
    private String phone2;
    private String email;
    private String web;
    private String birthday;

    // References to child nodes in the tree
    PeopleRecord left;
    PeopleRecord right;

    // Constructor
    public PeopleRecord(String givenName, String familyName, String companyName,
                        String address, String city, String county, String state,
                        String zip, String phone1, String phone2, String email,
                        String web, String birthday) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.companyName = companyName;
        this.address = address;
        this.city = city;
        this.county = county;
        this.state = state;
        this.zip = zip;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.email = email;
        this.web = web;
        this.birthday = birthday;
        this.left = null;
        this.right = null;
    }

    // Getters and Setters
    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public String getPhone1() { return phone1; }
    public void setPhone1(String phone1) { this.phone1 = phone1; }

    public String getPhone2() { return phone2; }
    public void setPhone2(String phone2) { this.phone2 = phone2; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWeb() { return web; }
    public void setWeb(String web) { this.web = web; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    // toString Method
    @Override
    public String toString() {
        return "PeopleRecord{" +
                "givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", county='" + county + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", phone1='" + phone1 + '\'' +
                ", phone2='" + phone2 + '\'' +
                ", email='" + email + '\'' +
                ", web='" + web + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }

    // compareTo Method
    public int compareTo(PeopleRecord other) {
        return this.familyName.compareTo(other.familyName);
    }
}





