import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './App.css';
import Login from './auth/Login';
import Register from './auth/Register';
import ForgotPassword from './auth/ForgotPassword';
import OtpVerify from './auth/OtpVerify';
import ResetPassword from './auth/ResetPassword';
import SettingsProfile from './pages/SettingsProfile';
import SettingsChangePassword from './pages/SettingsChangePassword';

function App() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/user/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path='/forgot/password' element={<ForgotPassword />} />
          <Route path='/otp/verify' element={<OtpVerify />} />
          <Route path='/reset/password' element={<ResetPassword />} />
          <Route path='/settings/profile' element={<SettingsProfile />} />
          <Route path='/settings/change-password' element={<SettingsChangePassword />} />
        </Routes>
      </BrowserRouter >
    </>
  );
}

export default App;