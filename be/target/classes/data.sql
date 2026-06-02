INSERT INTO permissions(permission_name, permission_description) VALUES
('USER_VIEW', 'Xem thông tin người dùng'),
('USER_CREATE', 'Tạo người dùng'),
('USER_UPDATE', 'Cập nhật người dùng'),
('USER_DELETE', 'Xóa người dùng'),

('ROLE_VIEW', 'Xem vai trò'),
('ROLE_CREATE', 'Tạo vai trò'),
('ROLE_UPDATE', 'Cập nhật vai trò'),
('ROLE_DELETE', 'Xóa vai trò'),

('PRODUCT_VIEW', 'Xem sản phẩm'),
('PRODUCT_CREATE', 'Tạo sản phẩm'),
('PRODUCT_UPDATE', 'Cập nhật sản phẩm'),
('PRODUCT_DELETE', 'Xóa sản phẩm'),

('ORDER_VIEW', 'Xem đơn hàng'),
('ORDER_CREATE', 'Tạo đơn hàng'),
('ORDER_UPDATE', 'Cập nhật đơn hàng'),
('ORDER_DELETE', 'Xóa đơn hàng'),

('CATEGORY_VIEW', 'Xem danh mục'),
('CATEGORY_CREATE', 'Tạo danh mục'),
('CATEGORY_UPDATE', 'Cập nhật danh mục'),
('CATEGORY_DELETE', 'Xóa danh mục');

INSERT INTO roles(role_name, role_description) VALUES
('ADMIN', 'Quản trị hệ thống'),
('STAFF', 'Nhân viên'),
('USER', 'Khách hàng');

INSERT INTO role_permissions(role_id, permission_id)
SELECT
(SELECT role_id FROM roles WHERE role_name = 'ADMIN'),
permission_id
FROM permissions;

INSERT INTO role_permissions(role_id, permission_id)
SELECT
(SELECT role_id FROM roles WHERE role_name = 'STAFF'),
permission_id
FROM permissions
WHERE permission_name IN (
'PRODUCT_VIEW',
'PRODUCT_CREATE',
'PRODUCT_UPDATE',

'ORDER_VIEW',
'ORDER_UPDATE',

'CATEGORY_VIEW',
'CATEGORY_CREATE',
'CATEGORY_UPDATE'
);


INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_name IN (
                                       'PRODUCT_VIEW',
                                       'ORDER_CREATE',
                                       'ORDER_VIEW'
                  )
WHERE r.role_name = 'CUSTOMER';