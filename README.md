# OOP LAB 8 - Delegate & Event (Java)

Bài quản lý thiết bị phòng học theo đề: điều khiển máy chiếu, điều khiển điều hòa,
micro không dây. Chuyển cú pháp C# trong đề sang Java theo mô hình functional interface
và listener. Không cần thư viện ngoài; dùng JDK 17 trở lên (đã kiểm tra với JDK 21).

## Chạy chương trình

Mở PowerShell tại thư mục dự án:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 -Xlint:all -d out src/lab8/*.java
java -cp out lab8.Main
```

Demo minh họa đổi trạng thái, cảnh báo theo ngưỡng, nhiều subscriber, callback chỉ nhận
Critical, ngoại lệ dữ liệu sai, lịch sử, yêu cầu bảo trì và hủy đăng ký sau `close()`.
Kết quả có 3 dòng lịch sử và 4 yêu cầu bảo trì. Sau dòng `After close` không có cảnh báo.

## Kiểm thử

Các kiểm thử nằm ngay trong `Main.java` và được `main()` chạy trước phần demo.
Chỉ cần chạy `lab8.Main` như trên; khi thành công sẽ in `PASS: 78 checks`.

Kiểm tra các ngưỡng pin/tín hiệu, chống cảnh báo lặp, dữ liệu ngoài khoảng,
thứ tự cập nhật trạng thái, đăng ký/hủy đăng ký, bộ lọc, danh sách chỉ đọc và listener lỗi.
Nếu kiểm thử thất bại, chương trình ném AssertionError và trả mã lỗi.

## Tổ chức mã nguồn

- `src/lab8`: mỗi lớp/interface/enum ở một file; bắt đầu đọc từ `Main.java`.
- `src/lab8/Main.java`: chứa cả demo và các phương thức kiểm thử, không phụ thuộc JUnit.
- [DESIGN.md](DESIGN.md): phân tích publisher/subscriber, UML, câu trả lời thiết kế,
  quy tắc vùng cảnh báo và chính sách xử lý ngoại lệ.

| C# trong đề | Java trong bài |
| --- | --- |
| delegate EventHandler<T> | EventHandler<T> với phương thức handle |
| event, +=, -= | Danh sách private, add/remove listener |
| delegate AlertFilter | @FunctionalInterface AlertFilter |
| Action<T> | Consumer<T> |
| EventArgs với thuộc tính chỉ đọc | record bất biến |
| protected virtual | Phương thức protected có thể override |
| IDisposable / using | AutoCloseable / try-with-resources |
| IReadOnlyList | Collections.unmodifiableList |

Giữ lại biến handler khi cần hủy đăng ký: hai lần viết `service::handleAlert` có thể tạo
hai đối tượng khác nhau. Coordinator đã lưu các handler để hủy đúng đăng ký của mình.
