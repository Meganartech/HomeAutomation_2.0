import { BrowserRouter, Routes, Route, useParams } from 'react-router-dom';
import './App.css';
import Login from './auth/Login';
import Register from './auth/Register';
import ForgotPassword from './auth/ForgotPassword';
import OtpVerify from './auth/OtpVerify';
import ResetPassword from './auth/ResetPassword';
import SettingsProfile from './pages/Settings/Profile.js';
import SettingsChangePassword from './pages/Settings/ChangePassword.js';
import SettingsDeviceSettings from './pages/Settings/DeviceSettings.js';
import SettingsMessageCenter from './pages/Settings/MessageCenter.js';
import SettingsFAQFeedback from './pages/Settings/FAQFeedback.js';
// import LivingRoom from './pages/Rooms/LivingRoom.js';
// import BedRoom from './pages/Rooms/BedRoom.js';
// import Hall from './pages/Rooms/Hall.js';
// import Kitchen from './pages/Rooms/Kitchen.js'
// import RestRoom from './pages/Rooms/RestRoom.js'
import RoomsLayout from './components/RoomsLayout';
import RoomContent from './components/RoomContent';


function App() {
  return (
    <>
      <div className='only-1440'>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/user/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path='/forgot/password' element={<ForgotPassword />} />
            <Route path='/otp/verify' element={<OtpVerify />} />
            <Route path='/reset/password' element={<ResetPassword />} />

            {/* <Route path='/room/living_room' element={<LivingRoom />} />
            <Route path='/room/bed_room' element={<BedRoom />} />
            <Route path='/room/hall' element={<Hall />} />
            <Route path='/room/kitchen' element={<Kitchen />} />
            <Route path='/room/rest_room' element={<RestRoom />} /> */}

            <Route path="/room/:roomName" element={<DynamicRoom />} />

            <Route path='/settings/profile_' element={<SettingsProfile />} />
            <Route path='/settings/change_password_' element={<SettingsChangePassword />} />
            <Route path='/settings/device_settings_' element={<SettingsDeviceSettings />} />
            <Route path='/settings/message_center_' element={<SettingsMessageCenter />} />
            <Route path='/settings/faq_feedback_' element={<SettingsFAQFeedback />} />


          </Routes>
        </BrowserRouter >
      </div>
    </>
  );
};

function DynamicRoom() {
  const { roomName } = useParams();
  const formattedRoomName = roomName.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  return <RoomsLayout roomName={formattedRoomName} ContentComponent={(props) => <RoomContent {...props} roomName={formattedRoomName} />} />;
}

export default App;