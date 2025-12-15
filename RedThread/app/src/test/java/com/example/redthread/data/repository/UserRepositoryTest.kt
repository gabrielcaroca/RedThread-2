package com.example.redthread.data.repository

import com.example.redthread.data.local.user.UserDao
import com.example.redthread.data.local.user.UserEntity
import com.example.redthread.domain.enums.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserRepositoryTest {

    private val userDao: UserDao = mock()
    private val repo = UserRepository(userDao)

    @Test
    fun `login returns success when credentials match`() = runTest {
        val user = UserEntity(
            id = 1,
            name = "Pipe",
            email = "pipe@email.com",
            phone = "12345678",
            password = "1234",
            role = UserRole.USUARIO
        )

        whenever(userDao.getByEmail("pipe@email.com")).thenReturn(user)

        val result = repo.login("pipe@email.com", "1234")

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        verify(userDao).getByEmail("pipe@email.com")
    }

    @Test
    fun `login returns failure when credentials are invalid`() = runTest {
        whenever(userDao.getByEmail("x@email.com")).thenReturn(null)

        val result = repo.login("x@email.com", "bad")

        assertTrue(result.isFailure)
        verify(userDao).getByEmail("x@email.com")
    }

    @Test
    fun `register returns failure when email already exists`() = runTest {
        val existing = UserEntity(
            id = 2,
            name = "Otro",
            email = "test@email.com",
            phone = "11111111",
            password = "pass",
            role = UserRole.USUARIO
        )
        whenever(userDao.getByEmail("test@email.com")).thenReturn(existing)

        val result = repo.register(
            name = "Pipe",
            email = "test@email.com",
            phone = "22222222",
            password = "1234"
        )

        assertTrue(result.isFailure)
        verify(userDao).getByEmail("test@email.com")
    }

    @Test
    fun `register inserts user and returns id when email does not exist`() = runTest {
        whenever(userDao.getByEmail("new@email.com")).thenReturn(null)
        whenever(userDao.insert(any())).thenReturn(10L)

        val result = repo.register(
            name = "Pipe",
            email = "new@email.com",
            phone = "22222222",
            password = "1234"
        )

        assertTrue(result.isSuccess)
        assertEquals(10L, result.getOrNull())
        verify(userDao).getByEmail("new@email.com")
        verify(userDao).insert(any())
    }
}
