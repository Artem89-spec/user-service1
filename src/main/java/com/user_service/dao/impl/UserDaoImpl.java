package com.user_service.dao.impl;

import com.user_service.config.HibernateConfig;
import com.user_service.dao.UserDao;
import com.user_service.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) {
        Transaction transaction = null;

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            logger.info("Пользователь успешно сохранен: {}", user.getEmail());
            return user;
        } catch (ConstraintViolationException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Повторяющийся email: {}", user.getEmail());
            throw new RuntimeException("Email уже существует в базе: " + e.getMessage());
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Пользователь не сохранен", e);
            throw new RuntimeException("Ошибка сохранения пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        Transaction transaction = null;

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User founUser = session.find(User.class, id);
            transaction.commit();
            logger.info("Пользователь успешно найден по id: {}", id);
            return Optional.ofNullable(founUser);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка поиска пользователя по id: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Transaction transaction = null;

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User founUser = session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();
            transaction.commit();
            logger.info("Пользователь успешно найден по email: {}", email);
            return Optional.ofNullable(founUser);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка поиска пользователя по email: {}", email, e);
            return Optional.empty();
        }
    }

    @Override
    public List<User> findByName(String name) {
        Transaction transaction = null;

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<User> results = session.createQuery("FROM User WHERE name LIKE :name ORDER BY id", User.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();
            transaction.commit();
            logger.info("Пользователи успешно найдены по имени: {}", name);
            return results;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка поиска пользователя по имени: {}", name, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<User> findAll() {
        Transaction transaction = null;

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<User> results = session.createQuery("FROM User ORDER BY id", User.class)
                    .getResultList();
            transaction.commit();
            logger.info("Пользователи успешно найдены");
            return results;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка получения списка пользователей", e);
            return Collections.emptyList();
        }
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User updatedUser = session.merge(user);
            transaction.commit();
            logger.info("Пользователь успешно обновлен: {}", user.getId());
            return updatedUser;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка обновления пользователя: {}", user.getId(), e);
            throw new RuntimeException("Ошибка обновления пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        Transaction transaction = null;

        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User foundUser = session.find(User.class, id);
            if (foundUser != null) {
                session.remove(foundUser);
                transaction.commit();
                logger.info("Пользователь успешно удален: {}", id);
                return true;
            }

            transaction.commit();
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка удаления пользователя: {}", id, e);
            throw new RuntimeException("Ошибка удаления пользователя: " + e.getMessage(), e);
        }
    }
}
