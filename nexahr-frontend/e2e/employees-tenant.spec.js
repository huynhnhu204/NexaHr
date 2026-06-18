import { test, expect } from '@playwright/test';
import { loginAs, loginViaApi, API } from './helpers.js';

test.describe('Employees & Multi-tenant', () => {
  test('employees list loads for admin', async ({ page }) => {
    await loginAs(page, 'admin@nexahr.com', '123456', 9);
    await page.goto('/employees');
    await expect(page.getByText('Đã xảy ra lỗi')).toHaveCount(0);
    await expect(page.locator('.ant-table, .data-table-card')).toBeVisible();
  });

  test('employee role sees 403 on employees route', async ({ page }) => {
    await loginAs(page, 'employee@nexahr.com', '123456', 9);
    await page.goto('/employees');
    await expect(page.getByText('Không có quyền truy cập')).toBeVisible({ timeout: 10_000 });
  });

  test('admin can list multiple companies', async () => {
    const session = await loginViaApi('admin@nexahr.com', '123456', 9);
    const res = await fetch(`${API}/companies/my`, {
      headers: { Authorization: `Bearer ${session.accessToken}` },
    });
    expect(res.ok).toBeTruthy();
    const json = await res.json();
    const companies = json.data ?? json;
    expect(companies.length).toBeGreaterThanOrEqual(2);
    const names = new Set(companies.map((c) => c.name));
    expect(names.size).toBeGreaterThan(1);
  });
});
