import java.io.*;
import java.util.*;

public class SortingEvaluation {
    
    // Sorting algorithm implementations
    public static void insertionSort(String[] arr) {
        for (int i = 1; i < arr.length; i++) {
            String key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j].compareTo(key) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    public static void quickSort(String[] arr) {
        quickSort(arr, 0, arr.length - 1);
    }

    private static void quickSort(String[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private static int partition(String[] arr, int low, int high) {
        String pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j].compareTo(pivot) <= 0) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    public static void mergeSort(String[] arr) {
        if (arr.length > 1) {
            int mid = arr.length / 2;
            String[] left = Arrays.copyOfRange(arr, 0, mid);
            String[] right = Arrays.copyOfRange(arr, mid, arr.length);
            mergeSort(left);
            mergeSort(right);
            merge(arr, left, right);
        }
    }

    private static void merge(String[] arr, String[] left, String[] right) {
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) {
            if (left[i].compareTo(right[j]) <= 0) {
                arr[k++] = left[i++];
            } else {
                arr[k++] = right[j++];
            }
        }
        while (i < left.length) arr[k++] = left[i++];
        while (j < right.length) arr[k++] = right[j++];
    }

    private static void swap(String[] arr, int i, int j) {
        String temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // Read CSV file
    public static String[] readCSV(String filename) throws IOException {
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                data.add(line.trim());
            }
        }
        return data.toArray(new String[0]);
    }

    // Performance testing framework
    public static long testSort(String[] data, String algorithm) {
        String[] copy = Arrays.copyOf(data, data.length);
        long start = System.nanoTime();
        switch (algorithm) {
            case "Insertion":
                insertionSort(copy);
                break;
            case "Quick":
                quickSort(copy);
                break;
            case "Merge":
                mergeSort(copy);
                break;
        }
        return System.nanoTime() - start;
    }

    public static void main(String[] args) {
        // List of datasets
        String[] datasets = {
            "1000places_sorted.csv",
            "1000places_random.csv",
            "10000places_sorted.csv",
            "10000places_random.csv"
        };

        // Results table header
        System.out.println("| Dataset               | Insertion (ns) | Quick (ns)   | Merge (ns)   |");
        System.out.println("|-----------------------|----------------|--------------|--------------|");

        for (String dataset : datasets) {
            try {
                String[] data = readCSV(dataset);
                long insertionTime = testSort(data, "Insertion");
                long quickTime = testSort(data, "Quick");
                long mergeTime = testSort(data, "Merge");

                System.out.printf("| %-21s | %14d | %12d | %12d |\n",
                        dataset, insertionTime, quickTime, mergeTime);
            } catch (IOException e) {
                System.err.println("Error reading file: " + dataset);
            }
        }
    }
}