import { BrowserRouter, Routes, Route, useParams } from 'react-router-dom';
import { Navigate } from 'react-router-dom';
import './App.css';
import Guard from './components/Guard';
import Login from './auth/Login';
import Register from './auth/Register';
import ForgotPassword from './auth/ForgotPassword';
import OtpVerify from './auth/OtpVerify';
import ResetPassword from './auth/ResetPassword';
import RoomsLayout from './components/layout/RoomsLayout';
import RoomsContent from './components/content/RoomsContent';
import ScheduleLayout from './components/layout/ScheduleLayout';
import YourScenesContent from './components/content/YourScenesContent';
import CreateScenesContent from './components/content/CreateScenesContent';
import HomeLayout from './components/layout/HomeLayout';
import HomeContent from './components/content/HomeContent';
import SettingsLayout from './components/layout/SettingsLayout';
import ProfileContent from './components/content/ProfileContent';
import ChangePasswordContent from './components/content/ChangePasswordContent';
import DevicesLayout from './components/layout/DevicesLayout';
import AllDevicesContent from './components/content/AllDevicesContent';
import SearchBindingsContent from './components/content/SearchBindingsContent';
import UpdateSceneContent from './components/content/UpdateScenesContent';

function App() {
  return (
    <div className='only-1440'>
      <BrowserRouter>
        <Guard>
          <Routes>

            {/* Auth routes */}
            <Route path='/' element={<Login />} />
            <Route path='/register' element={<Register />} />
            <Route path='/forgot/password' element={<ForgotPassword />} />
            <Route path='/otp/verify' element={<OtpVerify />} />
            <Route path='/reset/password' element={<ResetPassword />} />

            {/* Home */}
            <Route path='/home' element={<HomeLayout InsideContent={HomeContent} />} />

            {/* Dynamic Room */}
            <Route path='/room/:roomName' element={<DynamicRoom />} />

            {/* Devices */}
            <Route path='/devices/all_devices' element={<DevicesLayout activePage={'All Devices'} InsideContent={AllDevicesContent} />} />
            <Route path='/devices/search_bindings' element={<DevicesLayout activePage={'Search Bindings'} InsideContent={SearchBindingsContent} />} />

            {/* Schedule */}
            <Route path='/schedule/your_schedule' element={<ScheduleLayout activePage={'Your Schedule'} InsideContent={YourScenesContent} />} />
            <Route path='/schedule/create_schedule' element={<ScheduleLayout activePage={'Create Schedule'} InsideContent={CreateScenesContent} />} />
            <Route path="/schedule/update_schedule/:id" element={<ScheduleLayout activePage={'Create Schedule'} InsideContent={UpdateSceneContent} />} />

            {/* Settings */}
            <Route path='/settings/profile' element={<SettingsLayout activePage={'Profile'} InsideContent={ProfileContent} />} />
            <Route path='/settings/change_password' element={<SettingsLayout activePage={'Profile'} InsideContent={ChangePasswordContent} />} />

            <Route path="*" element={<Navigate to="/" replace />} />

          </Routes>
        </Guard>
      </BrowserRouter>
    </div>
  );
}

function DynamicRoom() {
  const { roomName } = useParams();
  const formattedRoomName = roomName.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  return (
    <RoomsLayout roomName={formattedRoomName} InsideContent={(props) => <RoomsContent {...props} roomName={formattedRoomName} />} />
  );
}

export default App;