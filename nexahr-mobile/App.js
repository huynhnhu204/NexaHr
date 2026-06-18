import { StatusBar } from 'expo-status-bar';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import {
  StyleSheet, Text, View, TouchableOpacity, Alert, TextInput,
  ActivityIndicator, FlatList, ScrollView, Modal,
} from 'react-native';
import { useEffect, useState, useCallback } from 'react';
import api from './src/api';

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

const LEAVE_TYPES = {
  ANNUAL_LEAVE: 'Nghỉ phép năm',
  SICK_LEAVE: 'Nghỉ ốm',
  UNPAID_LEAVE: 'Nghỉ không lương',
};

const ROLE_LABELS = {
  ADMIN: 'Quản trị viên',
  HR: 'Nhân sự',
  MANAGER: 'Quản lý',
  EMPLOYEE: 'Nhân viên',
};

function LoginScreen({ onLogin }) {
  const [email, setEmail] = useState('employee@nexahr.com');
  const [password, setPassword] = useState('123456');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    setLoading(true);
    try {
      const session = await api.login(email, password);
      await api.registerPush();
      const companies = await api.getCompanies();
      onLogin({ session, companies });
    } catch (e) {
      Alert.alert('Đăng nhập thất bại', e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.screen}>
      <Text style={styles.title}>NexaHR</Text>
      <Text style={styles.sub}>Work Smarter. Manage Better.</Text>
      <TextInput
        style={styles.input}
        value={email}
        onChangeText={setEmail}
        placeholder="Email"
        placeholderTextColor="#64748B"
        autoCapitalize="none"
        keyboardType="email-address"
      />
      <TextInput
        style={styles.input}
        value={password}
        onChangeText={setPassword}
        placeholder="Mật khẩu"
        placeholderTextColor="#64748B"
        secureTextEntry
      />
      <TouchableOpacity style={styles.btn} onPress={handleLogin} disabled={loading}>
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.btnText}>Đăng nhập</Text>}
      </TouchableOpacity>
      <Text style={styles.hint}>Demo: employee@nexahr.com / 123456</Text>
      <StatusBar style="light" />
    </View>
  );
}

function CompanyScreen({ companies, onSelect, onSkip }) {
  const [loading, setLoading] = useState(false);

  const select = async (companyId) => {
    setLoading(true);
    try {
      await api.switchCompany(companyId);
      onSelect();
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.screen}>
      <Text style={styles.title}>Chọn công ty</Text>
      <Text style={styles.sub}>Tài khoản của bạn thuộc nhiều công ty</Text>
      {loading ? <ActivityIndicator color="#60A5FA" /> : (
        <FlatList
          data={companies}
          keyExtractor={(item) => String(item.id)}
          renderItem={({ item }) => (
            <TouchableOpacity style={styles.card} onPress={() => select(item.id)}>
              <Text style={styles.cardTitle}>{item.name}</Text>
              <Text style={styles.cardText}>{item.code}</Text>
              {item.isDefault && <Text style={styles.badge}>Mặc định</Text>}
            </TouchableOpacity>
          )}
        />
      )}
      {companies.length <= 1 && (
        <TouchableOpacity style={styles.btn} onPress={onSkip}>
          <Text style={styles.btnText}>Tiếp tục</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}

function HomeScreen() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.getMobileSummary();
      setSummary(data);
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const attendanceLabel = () => {
    if (!summary) return '';
    if (summary.checkedOutToday) return '✓ Đã chấm ra';
    if (summary.checkedInToday) return '✓ Đã chấm vào';
    return 'Chưa chấm công hôm nay';
  };

  return (
    <ScrollView style={styles.screen} contentContainerStyle={styles.scrollContent}>
      <Text style={styles.title}>Trang chủ</Text>
      {loading && !summary ? <ActivityIndicator color="#60A5FA" /> : summary && (
        <View style={styles.card}>
          <Text style={styles.cardTitle}>Xin chào, {summary.fullName}</Text>
          {summary.companyName && <Text style={styles.cardText}>{summary.companyName}</Text>}
          <Text style={styles.cardText}>{attendanceLabel()}</Text>
          {summary.todayAttendanceStatus && (
            <Text style={styles.cardText}>Trạng thái: {summary.todayAttendanceStatus}</Text>
          )}
          <Text style={styles.cardText}>Đơn chờ duyệt: {summary.pendingLeaves}</Text>
          <Text style={styles.cardText}>Thông báo chưa đọc: {summary.unreadNotifications}</Text>
        </View>
      )}
      <TouchableOpacity
        style={styles.btn}
        disabled={summary?.checkedInToday}
        onPress={() => api.checkIn().then(() => { Alert.alert('OK', 'Chấm vào thành công'); load(); }).catch((e) => Alert.alert('Lỗi', e.message))}
      >
        <Text style={styles.btnText}>{summary?.checkedInToday ? 'Đã chấm vào' : 'Chấm vào'}</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={[styles.btn, styles.btnOutline]}
        disabled={!summary?.checkedInToday || summary?.checkedOutToday}
        onPress={() => api.checkOut().then(() => { Alert.alert('OK', 'Chấm ra thành công'); load(); }).catch((e) => Alert.alert('Lỗi', e.message))}
      >
        <Text style={[styles.btnText, styles.btnOutlineText]}>
          {summary?.checkedOutToday ? 'Đã chấm ra' : 'Chấm ra'}
        </Text>
      </TouchableOpacity>
      <TouchableOpacity style={[styles.btn, styles.btnGhost]} onPress={load}>
        <Text style={[styles.btnText, styles.btnOutlineText]}>Làm mới</Text>
      </TouchableOpacity>
      <StatusBar style="light" />
    </ScrollView>
  );
}

function LeaveScreen() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [leaveType, setLeaveType] = useState('ANNUAL_LEAVE');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.getMyLeaves();
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const submitLeave = async () => {
    if (!startDate || !endDate) {
      Alert.alert('Thiếu thông tin', 'Nhập ngày theo định dạng YYYY-MM-DD');
      return;
    }
    setSubmitting(true);
    try {
      await api.createLeave({ leaveType, startDate, endDate, reason: reason || undefined });
      Alert.alert('Thành công', 'Đã gửi đơn nghỉ phép');
      setModalOpen(false);
      setStartDate('');
      setEndDate('');
      setReason('');
      load();
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <View style={styles.screen}>
      <View style={styles.rowBetween}>
        <Text style={styles.title}>Nghỉ phép</Text>
        <TouchableOpacity style={styles.btnSmall} onPress={() => setModalOpen(true)}>
          <Text style={styles.btnText}>+ Tạo đơn</Text>
        </TouchableOpacity>
      </View>
      {loading ? <ActivityIndicator color="#60A5FA" /> : (
        <FlatList
          data={items}
          keyExtractor={(item) => String(item.id)}
          ListEmptyComponent={<Text style={styles.sub}>Chưa có đơn nghỉ phép</Text>}
          renderItem={({ item }) => (
            <View style={styles.card}>
              <Text style={styles.cardTitle}>{LEAVE_TYPES[item.leaveType] || item.leaveType} — {item.status}</Text>
              <Text style={styles.cardText}>{item.startDate} → {item.endDate}</Text>
              <Text style={styles.cardText}>{item.totalDays} ngày</Text>
              {item.reason && <Text style={styles.cardText}>{item.reason}</Text>}
            </View>
          )}
        />
      )}

      <Modal visible={modalOpen} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <Text style={styles.cardTitle}>Tạo đơn nghỉ phép</Text>
            <Text style={styles.label}>Loại nghỉ</Text>
            {Object.entries(LEAVE_TYPES).map(([key, label]) => (
              <TouchableOpacity
                key={key}
                style={[styles.chip, leaveType === key && styles.chipActive]}
                onPress={() => setLeaveType(key)}
              >
                <Text style={[styles.chipText, leaveType === key && styles.chipTextActive]}>{label}</Text>
              </TouchableOpacity>
            ))}
            <Text style={styles.label}>Từ ngày (YYYY-MM-DD)</Text>
            <TextInput style={styles.input} value={startDate} onChangeText={setStartDate} placeholder="2026-06-20" placeholderTextColor="#64748B" />
            <Text style={styles.label}>Đến ngày (YYYY-MM-DD)</Text>
            <TextInput style={styles.input} value={endDate} onChangeText={setEndDate} placeholder="2026-06-21" placeholderTextColor="#64748B" />
            <Text style={styles.label}>Lý do</Text>
            <TextInput style={[styles.input, styles.textArea]} value={reason} onChangeText={setReason} multiline placeholder="Tùy chọn" placeholderTextColor="#64748B" />
            <View style={styles.row}>
              <TouchableOpacity style={[styles.btn, styles.btnGhost, styles.flex]} onPress={() => setModalOpen(false)}>
                <Text style={[styles.btnText, styles.btnOutlineText]}>Hủy</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[styles.btn, styles.flex]} onPress={submitLeave} disabled={submitting}>
                {submitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.btnText}>Gửi đơn</Text>}
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

function NotificationsScreen() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.getNotifications();
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const markRead = async (id) => {
    try {
      await api.markNotificationRead(id);
      load();
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    }
  };

  const markAll = async () => {
    try {
      await api.markAllNotificationsRead();
      load();
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    }
  };

  return (
    <View style={styles.screen}>
      <View style={styles.rowBetween}>
        <Text style={styles.title}>Thông báo</Text>
        <TouchableOpacity onPress={markAll}>
          <Text style={styles.link}>Đọc tất cả</Text>
        </TouchableOpacity>
      </View>
      {loading ? <ActivityIndicator color="#60A5FA" /> : (
        <FlatList
          data={items}
          keyExtractor={(item) => String(item.id)}
          ListEmptyComponent={<Text style={styles.sub}>Không có thông báo</Text>}
          renderItem={({ item }) => (
            <TouchableOpacity
              style={[styles.card, !item.isRead && styles.cardUnread]}
              onPress={() => !item.isRead && markRead(item.id)}
            >
              <Text style={styles.cardTitle}>{item.title}</Text>
              <Text style={styles.cardText}>{item.message}</Text>
              <Text style={styles.muted}>{item.createdAt?.replace('T', ' ').slice(0, 16)}</Text>
            </TouchableOpacity>
          )}
        />
      )}
      <TouchableOpacity style={[styles.btn, styles.btnGhost]} onPress={load}>
        <Text style={[styles.btnText, styles.btnOutlineText]}>Làm mới</Text>
      </TouchableOpacity>
    </View>
  );
}

function PayrollScreen() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.getMyPayrolls();
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  return (
    <View style={styles.screen}>
      <Text style={styles.title}>Bảng lương</Text>
      {loading ? <ActivityIndicator color="#60A5FA" /> : (
        <FlatList
          data={items}
          keyExtractor={(item) => String(item.id)}
          ListEmptyComponent={<Text style={styles.sub}>Chưa có bảng lương</Text>}
          renderItem={({ item }) => (
            <View style={styles.card}>
              <Text style={styles.cardTitle}>Tháng {item.salaryMonth || `${item.month}/${item.year}`}</Text>
              <Text style={styles.cardText}>Trạng thái: {item.status}</Text>
              <Text style={styles.cardText}>Thực lĩnh: {Number(item.netSalary || 0).toLocaleString('vi-VN')}đ</Text>
            </View>
          )}
        />
      )}
      <TouchableOpacity style={[styles.btn, styles.btnGhost]} onPress={load}>
        <Text style={[styles.btnText, styles.btnOutlineText]}>Làm mới</Text>
      </TouchableOpacity>
    </View>
  );
}

function ProfileScreen({ onLogout, navigation }) {
  const [profile, setProfile] = useState(null);
  const [summary, setSummary] = useState(null);
  const [companies, setCompanies] = useState([]);
  const [switching, setSwitching] = useState(false);

  const load = useCallback(async () => {
    try {
      const [me, sum, comps] = await Promise.all([
        api.getMe(),
        api.getMobileSummary(),
        api.getCompanies(),
      ]);
      setProfile(me);
      setSummary(sum);
      setCompanies(comps);
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const switchCompany = async (companyId) => {
    setSwitching(true);
    try {
      await api.switchCompany(companyId);
      Alert.alert('OK', 'Đã chuyển công ty');
      load();
    } catch (e) {
      Alert.alert('Lỗi', e.message);
    } finally {
      setSwitching(false);
    }
  };

  const logout = async () => {
    await api.logout();
    onLogout();
  };

  return (
    <ScrollView style={styles.screen} contentContainerStyle={styles.scrollContent}>
      <Text style={styles.title}>Hồ sơ</Text>
      {profile && (
        <View style={styles.card}>
          <Text style={styles.cardTitle}>{profile.fullName || profile.email}</Text>
          <Text style={styles.cardText}>{profile.email}</Text>
          <Text style={styles.cardText}>Vai trò: {ROLE_LABELS[profile.role] || profile.role}</Text>
          {summary?.companyName && <Text style={styles.cardText}>Công ty: {summary.companyName}</Text>}
        </View>
      )}
      {companies.length > 1 && (
        <>
          <Text style={styles.sectionTitle}>Chuyển công ty</Text>
          {switching ? <ActivityIndicator color="#60A5FA" /> : companies.map((c) => (
            <TouchableOpacity key={c.id} style={styles.card} onPress={() => switchCompany(c.id)}>
              <Text style={styles.cardTitle}>{c.name}</Text>
              <Text style={styles.cardText}>{c.code}</Text>
            </TouchableOpacity>
          ))}
        </>
      )}
      <TouchableOpacity style={styles.btn} onPress={() => navigation.navigate('Payroll')}>
        <Text style={styles.btnText}>Xem bảng lương</Text>
      </TouchableOpacity>
      <TouchableOpacity style={[styles.btn, styles.btnDanger]} onPress={logout}>
        <Text style={styles.btnText}>Đăng xuất</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

function MainTabs({ onLogout }) {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: '#60A5FA',
        tabBarStyle: { backgroundColor: '#0F172A', borderTopColor: '#1E293B' },
        tabBarInactiveTintColor: '#94A3B8',
      }}
    >
      <Tab.Screen name="Home" component={HomeScreen} options={{ title: 'Trang chủ' }} />
      <Tab.Screen name="Leave" component={LeaveScreen} options={{ title: 'Nghỉ phép' }} />
      <Tab.Screen name="Notifications" component={NotificationsScreen} options={{ title: 'Thông báo' }} />
      <Tab.Screen name="Payroll" component={PayrollScreen} options={{ title: 'Lương' }} />
      <Tab.Screen name="Profile">
        {(props) => <ProfileScreen {...props} onLogout={onLogout} />}
      </Tab.Screen>
    </Tab.Navigator>
  );
}

export default function App() {
  const [ready, setReady] = useState(false);
  const [authed, setAuthed] = useState(false);
  const [needsCompanyPick, setNeedsCompanyPick] = useState(false);
  const [companies, setCompanies] = useState([]);

  useEffect(() => {
    (async () => {
      try {
        const user = await api.validateSession();
        if (user) {
          const comps = await api.getCompanies();
          setCompanies(comps);
          setNeedsCompanyPick(comps.length > 1);
          setAuthed(true);
        }
      } catch {
        await api.logout();
      } finally {
        setReady(true);
      }
    })();
  }, []);

  const handleLogin = ({ companies: comps }) => {
    setCompanies(comps || []);
    setNeedsCompanyPick((comps || []).length > 1);
    setAuthed(true);
  };

  const handleCompanyDone = () => setNeedsCompanyPick(false);

  if (!ready) {
    return (
      <View style={styles.screen}>
        <ActivityIndicator color="#60A5FA" size="large" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        {!authed ? (
          <Stack.Screen name="Login">
            {() => <LoginScreen onLogin={handleLogin} />}
          </Stack.Screen>
        ) : needsCompanyPick ? (
          <Stack.Screen name="Company">
            {() => (
              <CompanyScreen
                companies={companies}
                onSelect={handleCompanyDone}
                onSkip={handleCompanyDone}
              />
            )}
          </Stack.Screen>
        ) : (
          <Stack.Screen name="Main">
            {() => <MainTabs onLogout={() => { setAuthed(false); setNeedsCompanyPick(false); }} />}
          </Stack.Screen>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: '#0F172A', padding: 24, paddingTop: 60 },
  scrollContent: { paddingBottom: 40 },
  title: { color: '#fff', fontSize: 24, fontWeight: '700' },
  sub: { color: '#94A3B8', marginBottom: 24 },
  hint: { color: '#64748B', fontSize: 12, marginTop: 8 },
  sectionTitle: { color: '#CBD5E1', fontWeight: '600', marginBottom: 8, marginTop: 8 },
  label: { color: '#94A3B8', marginBottom: 6, marginTop: 8 },
  input: {
    backgroundColor: '#1E293B',
    color: '#fff',
    borderRadius: 10,
    padding: 14,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#334155',
  },
  textArea: { minHeight: 72, textAlignVertical: 'top' },
  card: { backgroundColor: '#1E293B', borderRadius: 12, padding: 16, marginBottom: 12, gap: 6 },
  cardUnread: { borderLeftWidth: 3, borderLeftColor: '#60A5FA' },
  cardTitle: { color: '#fff', fontWeight: '600', fontSize: 16 },
  cardText: { color: '#CBD5E1' },
  muted: { color: '#64748B', fontSize: 12 },
  badge: { color: '#60A5FA', fontSize: 12 },
  btn: { backgroundColor: '#1E3A8A', padding: 14, borderRadius: 10, alignItems: 'center', marginBottom: 12 },
  btnSmall: { backgroundColor: '#1E3A8A', paddingHorizontal: 14, paddingVertical: 8, borderRadius: 8 },
  btnText: { color: '#fff', fontWeight: '600' },
  btnOutline: { backgroundColor: 'transparent', borderWidth: 1, borderColor: '#1E3A8A' },
  btnOutlineText: { color: '#60A5FA' },
  btnGhost: { backgroundColor: 'transparent', borderWidth: 1, borderColor: '#334155' },
  btnDanger: { backgroundColor: '#DC2626', marginTop: 8 },
  row: { flexDirection: 'row', gap: 12 },
  rowBetween: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  flex: { flex: 1 },
  link: { color: '#60A5FA', fontWeight: '600' },
  chip: { paddingVertical: 8, paddingHorizontal: 12, borderRadius: 8, borderWidth: 1, borderColor: '#334155', marginBottom: 8 },
  chipActive: { backgroundColor: '#1E3A8A', borderColor: '#1E3A8A' },
  chipText: { color: '#94A3B8' },
  chipTextActive: { color: '#fff' },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'flex-end' },
  modalCard: { backgroundColor: '#0F172A', borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 24, paddingBottom: 40, maxHeight: '90%' },
});
