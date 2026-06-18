import { test, expect } from '@playwright/test';
import { loginAs, FRONTEND } from './helpers.js';

test.describe('Auth flows', () => {
  test('login page renders', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('button', { name: 'Đăng nhập', exact: true })).toBeVisible();
    await expect(page.getByPlaceholder('admin@nexahr.com')).toBeVisible();
  });

  test('admin can access dashboard after session inject', async ({ page }) => {
    await loginAs(page, 'admin@nexahr.com', '123456', 9);
    await page.goto('/dashboard');
    await expect(page.getByText('Tổng quan').first()).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText('Đã xảy ra lỗi')).toHaveCount(0);
  });

  test('employee accounts load attendance', async ({ page }) => {
    await loginAs(page, 'employee@nexahr.com', '123456', 9);
    await page.goto('/attendance');
    await expect(page.getByRole('heading', { name: 'Chấm công', exact: true })).toBeVisible();
    await expect(page.getByText('Đã xảy ra lỗi')).toHaveCount(0);
  });

  test('unauthenticated user redirects to login', async ({ page }) => {
    await page.goto('/login');
    await page.evaluate(() => localStorage.clear());
    await page.goto('/employees');
    await expect(page).toHaveURL(/\/login/);
  });
});
