import Profile from '../../components/ProfileContent';
import SettingsLayout from '../../components/SettingsLayout';

export default function SettingsProfile() {
    return <SettingsLayout activeInsidePage="Profile" InsideContent={<Profile />} />;
}