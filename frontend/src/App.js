import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import Home from './Component/Home';
import Login from './Component/User/Login';
import Register from './Component/User/Register';
import Profile from './Component/User/Profile';
import ProfileUpdate from './Component/User/ProfileUpdate';
import AddThing from './Component/User/AddThing';
import AddRoom from './Component/User/Room';
import DeviceScanner from './Component/User/Scanning';
import AdminLogin from './Component/Admin/Login';
import AdminProfile from './Component/Admin/Profile';
import AdminProfileUpdate from './Component/Admin/ProfileUpdate';
import AdminUserList from './Component/Admin/UserList';
import ProtectedRoute from './Component/ProtectedRoute';
import ForgotPassword from './Component/ForgotPassword';
import VerifyOtp from './Component/VerifyOtp';
import ResetPassword from './Component/ResetPassword';

export default function App() {
  return (
    <>
      <Router>
        <Routes>
          <Route path='/' element={<Home />} />
          <Route path='/user/login' element={<Login />} />
          <Route path='/user/register' element={<Register />} />
          <Route path='/forgot/password' element={<ForgotPassword />} />
          <Route path='/verify/otp' element={<VerifyOtp />} />
          <Route path='/reset/password' element={<ResetPassword />} />

          {/* Protected User Routes */}
          <Route path='/user/profile' element={
            <ProtectedRoute role="USER">
              <Profile />
            </ProtectedRoute>
          } />
          <Route path='/user/profile/update' element={
            <ProtectedRoute role="USER">
              <ProfileUpdate />
            </ProtectedRoute>
          } />
          <Route path='/user/room' element={
            <ProtectedRoute role="USER">
              <AddRoom />
            </ProtectedRoute>
          } />
          <Route path='/user/device' element={
            <ProtectedRoute role="USER">
              <AddThing />
            </ProtectedRoute>
          } />
          <Route path='/user/scan' element={
            <ProtectedRoute role="USER">
              <DeviceScanner />
            </ProtectedRoute>
          } />

          {/* Admin Routes */}
          <Route path='/admin/login' element={<AdminLogin />} />
          <Route path='/admin/profile' element={
            <ProtectedRoute role="ADMIN">
              <AdminProfile />
            </ProtectedRoute>
          } />
          <Route path='/admin/profile/update' element={
            <ProtectedRoute role="ADMIN">
              <AdminProfileUpdate />
            </ProtectedRoute>
          } />
          <Route path='/admin/user/list' element={
            <ProtectedRoute role="ADMIN">
              <AdminUserList />
            </ProtectedRoute>
          } />

          {/* Catch-all route */}
          <Route path='*' element={<Home />} />
        </Routes>
      </Router>
    </>
  );
}
