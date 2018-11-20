/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.util.collections;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.util.Checks;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
class ImmutableArray<E> implements ImmutableList<E> {

    @Contract("null -> fail")
    static <E> @NotNull ImmutableList<@NotNull E> of(final @Nullable Object @Nullable ... elements) {
        return new ImmutableArray<>(Checks.elementsNotNull(elements, "Immutable list elements"));
    }

    private final @NotNull Object @NotNull [] array;

    ImmutableArray(final @NotNull Object @NotNull [] array) {
        this.array = array;
        assert size() > 1;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public @NotNull E get(final int index) {
        //noinspection unchecked
        return (E) array[Checks.index(index, array.length)];
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return array.clone();
    }

    @Override
    public <T> T @NotNull [] toArray(final T @NotNull [] other) {
        return toArray(array, 0, array.length, other);
    }

    private static <T> T @NotNull [] toArray(
            final @NotNull Object @NotNull [] array, final int fromIndex, final int length, T @NotNull [] other) {

        Checks.notNull(other, "Array");
        if (other.length < length) {
            //noinspection unchecked
            other = (T[]) Array.newInstance(other.getClass().getComponentType(), length);
        } else if (other.length > length) {
            other[length] = null;
        }
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(array, fromIndex, other, 0, length);
        return other;
    }

    @Override
    public int indexOf(final @Nullable Object o) {
        if (o == null) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (o.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(final @Nullable Object o) {
        if (o == null) {
            return -1;
        }
        for (int i = array.length - 1; i >= 0; i--) {
            if (o.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public @NotNull ImmutableListIterator<E> listIterator(final int index) {
        return new ArrayIterator(Checks.cursorIndex(index, array.length));
    }

    @Override
    public @NotNull Spliterator<E> spliterator() {
        return Spliterators.spliterator(array, Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
    }

    @Override
    public void forEach(final @NotNull Consumer<? super E> consumer) {
        Checks.notNull(consumer, "Consumer");
        for (int i = 0; i < array.length; i++) {
            consumer.accept(get(i));
        }
    }

    @Override
    public @NotNull ImmutableList<E> subList(final int fromIndex, final int toIndex) {
        Checks.indexRange(fromIndex, toIndex, array.length);
        final int size = toIndex - fromIndex;
        switch (size) {
            case 0:
                return ImmutableEmptyList.of();
            case 1:
                return new ImmutableElement<>(get(fromIndex));
            default:
                return (size == array.length) ? this : new SubArray(fromIndex, toIndex);
        }
    }

    @Override
    public @NotNull String toString() {
        return Arrays.toString(array);
    }

    private class SubArray implements ImmutableList<E> {

        private final int fromIndex;
        private final int toIndex;

        SubArray(final int fromIndex, final int toIndex) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            assert size() > 1;
            assert size() < ImmutableArray.this.size();
        }

        @Override
        public int size() {
            return toIndex - fromIndex;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public @NotNull E get(final int index) {
            return ImmutableArray.this.get(fromIndex + index);
        }

        @Override
        public @NotNull Object @NotNull [] toArray() {
            return Arrays.copyOfRange(array, fromIndex, toIndex);
        }

        @Override
        public <T> T @NotNull [] toArray(final T @NotNull [] other) {
            return ImmutableArray.toArray(array, fromIndex, size(), other);
        }

        @Override
        public int indexOf(final @Nullable Object o) {
            if (o == null) {
                return -1;
            }
            for (int i = fromIndex; i < toIndex; i++) {
                if (o.equals(array[i])) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(final @Nullable Object o) {
            if (o == null) {
                return -1;
            }
            for (int i = toIndex - 1; i >= fromIndex; i--) {
                if (o.equals(array[i])) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public @NotNull ImmutableListIterator<E> listIterator(final int index) {
            return new ArrayIterator(fromIndex, toIndex, Checks.cursorIndex(index, size()));
        }

        @Override
        public @NotNull Spliterator<E> spliterator() {
            return Spliterators.spliterator(
                    array, fromIndex, toIndex, Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
        }

        @Override
        public void forEach(final @NotNull Consumer<? super E> consumer) {
            Checks.notNull(consumer, "Consumer");
            for (int i = fromIndex; i < toIndex; i++) {
                consumer.accept(get(i));
            }
        }

        @Override
        public @NotNull ImmutableList<E> subList(final int fromIndex, final int toIndex) {
            return ImmutableArray.this.subList(this.fromIndex + fromIndex, this.fromIndex + toIndex);
        }

        @Override
        public @NotNull ImmutableList<E> trim() {
            return new ImmutableArray<>(toArray());
        }

        @Override
        public @NotNull String toString() {
            final StringBuilder sb = new StringBuilder().append('[');
            int i = fromIndex;
            while (true) {
                sb.append(array[i++]);
                if (i == toIndex) {
                    return sb.append(']').toString();
                }
                sb.append(',').append(' ');
            }
        }
    }

    private class ArrayIterator implements ImmutableListIterator<E> {

        private final int fromIndex;
        private final int toIndex;
        private int index;

        private ArrayIterator(final int index) {
            this.fromIndex = 0;
            this.toIndex = array.length;
            this.index = index;
        }

        private ArrayIterator(final int fromIndex, final int toIndex, final int index) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < toIndex;
        }

        @Override
        public @NotNull E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return get(index++);
        }

        @Override
        public boolean hasPrevious() {
            return index > fromIndex;
        }

        @Override
        public @NotNull E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            return get(--index);
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }
    }
}
