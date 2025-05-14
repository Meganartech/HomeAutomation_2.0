import ChangePasswordContent from '../../components/ChangePasswordContent';
import SettingsLayout from '../../components/SettingsLayout';

export default function SettingsProfile() {
    return <SettingsLayout activeInsidePage="Profile" InsideContent={<ChangePasswordContent />} />;
}