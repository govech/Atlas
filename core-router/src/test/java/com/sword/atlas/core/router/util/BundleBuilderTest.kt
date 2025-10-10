package com.sword.atlas.core.router.util

import android.content.Intent
import android.os.Bundle
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.Serializable

/**
 * BundleBuilder类单元测试
 * 测试Bundle构建器功能
 */
class BundleBuilderTest {

    private lateinit var bundleBuilder: BundleBuilder

    @Before
    fun setUp() {
        bundleBuilder = BundleBuilder.create()
    }

    @Test
    fun `test create factory method`() {
        // When
        val builder = BundleBuilder.create()

        // Then
        assertNotNull(builder)
        assertTrue(builder is BundleBuilder)
    }

    @Test
    fun `test putString and build`() {
        // Given
        val key = "name"
        val value = "test"

        // When
        val bundle = bundleBuilder
            .putString(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getString(key))
    }

    @Test
    fun `test putString with null value`() {
        // Given
        val key = "name"

        // When
        val bundle = bundleBuilder
            .putString(key, null)
            .build()

        // Then
        assertNull(bundle.getString(key))
    }

    @Test
    fun `test putInt and build`() {
        // Given
        val key = "age"
        val value = 25

        // When
        val bundle = bundleBuilder
            .putInt(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getInt(key))
    }

    @Test
    fun `test putLong and build`() {
        // Given
        val key = "timestamp"
        val value = 1234567890L

        // When
        val bundle = bundleBuilder
            .putLong(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getLong(key))
    }

    @Test
    fun `test putFloat and build`() {
        // Given
        val key = "price"
        val value = 99.99f

        // When
        val bundle = bundleBuilder
            .putFloat(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getFloat(key), 0.001f)
    }

    @Test
    fun `test putDouble and build`() {
        // Given
        val key = "latitude"
        val value = 39.9042

        // When
        val bundle = bundleBuilder
            .putDouble(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getDouble(key), 0.0001)
    }

    @Test
    fun `test putBoolean and build`() {
        // Given
        val key = "isEnabled"
        val value = true

        // When
        val bundle = bundleBuilder
            .putBoolean(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getBoolean(key))
    }

    @Test
    fun `test putSerializable and build`() {
        // Given
        val key = "data"
        val value = TestSerializable("test")

        // When
        val bundle = bundleBuilder
            .putSerializable(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getSerializable(key))
    }

    @Test
    fun `test putParcelable and build`() {
        // Given
        val key = "intent"
        val value = mockk<Intent>()

        // When
        val bundle = bundleBuilder
            .putParcelable(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getParcelable<Intent>(key))
    }

    @Test
    fun `test putStringArray and build`() {
        // Given
        val key = "tags"
        val value = arrayOf("tag1", "tag2", "tag3")

        // When
        val bundle = bundleBuilder
            .putStringArray(key, value)
            .build()

        // Then
        assertArrayEquals(value, bundle.getStringArray(key))
    }

    @Test
    fun `test putIntArray and build`() {
        // Given
        val key = "numbers"
        val value = intArrayOf(1, 2, 3)

        // When
        val bundle = bundleBuilder
            .putIntArray(key, value)
            .build()

        // Then
        assertArrayEquals(value, bundle.getIntArray(key))
    }

    @Test
    fun `test putLongArray and build`() {
        // Given
        val key = "ids"
        val value = longArrayOf(1L, 2L, 3L)

        // When
        val bundle = bundleBuilder
            .putLongArray(key, value)
            .build()

        // Then
        assertArrayEquals(value, bundle.getLongArray(key))
    }

    @Test
    fun `test putFloatArray and build`() {
        // Given
        val key = "prices"
        val value = floatArrayOf(1.1f, 2.2f, 3.3f)

        // When
        val bundle = bundleBuilder
            .putFloatArray(key, value)
            .build()

        // Then
        assertArrayEquals(value, bundle.getFloatArray(key), 0.001f)
    }

    @Test
    fun `test putDoubleArray and build`() {
        // Given
        val key = "coordinates"
        val value = doubleArrayOf(1.1, 2.2, 3.3)

        // When
        val bundle = bundleBuilder
            .putDoubleArray(key, value)
            .build()

        // Then
        assertArrayEquals(value, bundle.getDoubleArray(key), 0.0001)
    }

    @Test
    fun `test putBooleanArray and build`() {
        // Given
        val key = "flags"
        val value = booleanArrayOf(true, false, true)

        // When
        val bundle = bundleBuilder
            .putBooleanArray(key, value)
            .build()

        // Then
        assertArrayEquals(value, bundle.getBooleanArray(key))
    }

    @Test
    fun `test putParcelableArrayList and build`() {
        // Given
        val key = "intents"
        val value = arrayListOf<Intent>(mockk(), mockk())

        // When
        val bundle = bundleBuilder
            .putParcelableArrayList(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getParcelableArrayList<Intent>(key))
    }

    @Test
    fun `test putStringArrayList and build`() {
        // Given
        val key = "names"
        val value = arrayListOf("name1", "name2", "name3")

        // When
        val bundle = bundleBuilder
            .putStringArrayList(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getStringArrayList(key))
    }

    @Test
    fun `test putIntegerArrayList and build`() {
        // Given
        val key = "numbers"
        val value = arrayListOf(1, 2, 3)

        // When
        val bundle = bundleBuilder
            .putIntegerArrayList(key, value)
            .build()

        // Then
        assertEquals(value, bundle.getIntegerArrayList(key))
    }

    @Test
    fun `test putAll and build`() {
        // Given
        val existingBundle = Bundle().apply {
            putString("name", "test")
            putInt("age", 25)
        }

        // When
        val bundle = bundleBuilder
            .putAll(existingBundle)
            .build()

        // Then
        assertEquals("test", bundle.getString("name"))
        assertEquals(25, bundle.getInt("age"))
    }

    @Test
    fun `test chaining methods`() {
        // When
        val result = bundleBuilder
            .putString("name", "test")
            .putInt("age", 25)
            .putBoolean("isEnabled", true)

        // Then
        assertSame(bundleBuilder, result)
    }

    @Test
    fun `test build creates copy`() {
        // Given
        bundleBuilder.putString("name", "test")

        // When
        val bundle1 = bundleBuilder.build()
        val bundle2 = bundleBuilder.build()

        // Then
        assertNotSame(bundle1, bundle2)
        assertEquals(bundle1.getString("name"), bundle2.getString("name"))
    }

    @Test
    fun `test multiple values in same bundle`() {
        // When
        val bundle = bundleBuilder
            .putString("name", "John")
            .putInt("age", 30)
            .putBoolean("isActive", true)
            .putFloat("score", 95.5f)
            .putStringArray("tags", arrayOf("tag1", "tag2"))
            .build()

        // Then
        assertEquals("John", bundle.getString("name"))
        assertEquals(30, bundle.getInt("age"))
        assertTrue(bundle.getBoolean("isActive"))
        assertEquals(95.5f, bundle.getFloat("score"), 0.001f)
        assertArrayEquals(arrayOf("tag1", "tag2"), bundle.getStringArray("tags"))
    }

    @Test
    fun `test null values handling`() {
        // When
        val bundle = bundleBuilder
            .putString("nullString", null)
            .putStringArray("nullArray", null)
            .putSerializable("nullSerializable", null)
            .putParcelable("nullParcelable", null)
            .build()

        // Then
        assertNull(bundle.getString("nullString"))
        assertNull(bundle.getStringArray("nullArray"))
        assertNull(bundle.getSerializable("nullSerializable"))
        assertNull(bundle.getParcelable<Intent>("nullParcelable"))
    }

    // 测试用的Serializable类
    data class TestSerializable(val value: String) : Serializable
}