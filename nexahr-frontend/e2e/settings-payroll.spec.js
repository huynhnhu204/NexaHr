import { test, expect } from '@playwright/test';
import { loginAs } from './helpers.js';

test.describe('Settings & Payroll', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin@nexahr.com', '123456', 9);
  });

  test('settings page loads', async ({ page }) => {
    await page.goto('/settings');
    await expect(page.getByText('Thông tin tài khoản', { exact: true })).toBeVisible();
    await expect(page.getByText('Giao diện', { exact: true })).toBeVisible();
    await expect(page.getByText('Đã xảy ra lỗi')).toHaveCount(0);
  });

  test('payroll page loads for HR admin', async ({ page }) => {
    await page.goto('/payroll');
    await expect(page.getByText('Đã xảy ra lỗi')).toHaveCount(0);
  });

  test('positions page loads', async ({ page }) => {
    await page.goto('/positions');
    await expect(page.getByText('Đã xảy ra lỗi')).toHaveCount(0);
  });
});
