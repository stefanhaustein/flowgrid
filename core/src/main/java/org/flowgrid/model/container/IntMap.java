package org.flowgrid.model.container;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class IntMap<T> {
  private static final int INITIAL_SIZE = 8;
  private int[] keys = new int[INITIAL_SIZE];
  private T[] values = (T[]) new Object[INITIAL_SIZE];
  private Collection<T> valueCollection;

  /**
   * Number of keys, including keys freed up via removal or storing null.
   */
  private int keyCount;

  /**
   * Number of non-null values stored.
   */
  private int valueCount;

  public void put(int key, T value) {
    int pos = Arrays.binarySearch(keys, 0, keyCount, key);
    if (pos >= 0) {
      Object oldValue = values[pos];
      if (oldValue == null) {
        valueCount++;
      }
      if (value == null) {
        valueCount--;
      }
      values[pos] = value;
    } else if (value != null) {
      pos = (-pos) - 1;
      if (keyCount < keys.length) {
        // TODO: Find the closest gap if valueCount < keyCount?
        System.arraycopy(keys, pos, keys, pos + 1, keyCount - pos);
        System.arraycopy(values, pos, values, pos + 1, keyCount - pos);
        keyCount++;
        valueCount++;
        keys[pos] = key;
        values[pos] = value;
      } else {
        T[] newValues = (T[]) new Object[valueCount * 2];
        int[] newKeys = new int[newValues.length];
        int d = 0;
        for (int s = 0; s < pos; s++) {
          T v = values[s];
          if (v != null) {
            newValues[d] = v;
            newKeys[d] = keys[s];
            d++;
          }
        }
        newKeys[d] = key;
        newValues[d] = value;
        d++;
        for (int s = pos; s < keyCount; s++) {
          T v = values[s];
          if (v != null) {
            newValues[d] = v;
            newKeys[d] = keys[s];
            d++;
          }
        }
        keys = newKeys;
        values = newValues;
        keyCount = valueCount = d;
      }
    }
  }

  public int firstKey() {
    if (valueCount == 0) {
      throw new IllegalStateException();
    }
    for (int i = 0; i < keyCount; i++) {
      if (values[i] != null) {
        return keys[i];
      }
    }
    throw new IllegalStateException("Impossible");
  }

  public int lastKey() {
    if (valueCount == 0) {
      throw new IllegalStateException();
    }
    for (int i = keyCount - 1; i >= 0; i--) {
      if (values[i] != null) {
        return keys[i];
      }
    }
    throw new IllegalStateException("Impossible");
  }

  public void remove(int key) {
    put(key, null);
  }

  public T get(int key) {
    int pos = Arrays.binarySearch(keys, 0, keyCount, key);
    return pos >= 0 ? values[pos] : null;
  }

  public boolean containsKey(int key) {
    return Arrays.binarySearch(keys, 0, keyCount, key) >= 0;
  }

  public int size() {
    return valueCount;
  }

  public boolean isEmpty() {
    return valueCount == 0;
  }

  public Collection<T> values() {
    if (valueCollection == null) {
      valueCollection = new ValueCollection();
    }
    return valueCollection;
  }

  private class ValueCollection implements Collection<T> {
    @Override
    public boolean add(T object) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object object) {
      return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
      for (Object o: collection) {
        if (!contains(o)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean isEmpty() {
      return valueCount == 0;
    }

    @Override
    public Iterator<T> iterator() {
      return new Iterator<T>() {
        T next;
        int pos = 0;

        @Override
        public boolean hasNext() {
          if (next != null) {
            return true;
          }
          while (pos < keyCount) {
            T v = values[pos++];
            if (v != null) {
              next = v;
              return true;
            }
          }
          return false;
        }

        @Override
        public T next() {
          if (!hasNext()) {
            throw new IllegalStateException();
          }
          T result = next;
          next = null;
          return result;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public boolean remove(Object object) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
      return valueCount;
    }

    @Override
    public Object[] toArray() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T1> T1[] toArray(T1[] array) {
      throw new UnsupportedOperationException();
    }
  }
}
