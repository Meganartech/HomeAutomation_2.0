import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './App.css';
import Login from './auth/Login';
import Register from './auth/Register';
import ForgotPassword from './auth/ForgotPassword';
import OtpVerify from './auth/OtpVerify';
import ResetPassword from './auth/ResetPassword';
import Settings from './pages/Settings';

function App() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path='/forgot/password' element={<ForgotPassword />} />
          <Route path='/otp/verify' element={<OtpVerify />} />
          <Route path='/reset/password' element={<ResetPassword />} />
          <Route path='/settings' element={<Settings />} />
        </Routes>
      </BrowserRouter >
    </>
  );
}

export default App;
