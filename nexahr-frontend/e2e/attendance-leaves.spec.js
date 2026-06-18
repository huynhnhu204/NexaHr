import { test, expect } from '@playwright/test';
import { loginAs } from './helpers.js';

test.describe('Attendance & Leave', () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page, 'admin@nexahr.com', '123456', 9);
  });

  test('attendance page loads with table', async ({ page }) => {
    await page.goto('/attendance');
    await expect(page.getByText('Chấm công hôm nay')).toBeVisible();
    await expect(page.getByText('Đã xảy ra lỗi')).toHaveCount(0);
  });

  test('leaves page loads without crash', async ({ page }) => {
    await page.goto('/leaves');
    await expect(page.getByText('Quản lý nghỉ phép')).toBeVisible();
    await expect(page.getByText('Đã xảy ra lỗi')).toHaveCount(0);
  });

  test('leave modal opens', async ({ page }) => {
    await page.goto('/leaves');
    await page.getByRole('button', { name: 'Tạo đơn nghỉ phép' }).click();
    await expect(page.getByText('Loại nghỉ phép')).toBeVisible();
  });
});
